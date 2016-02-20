package com.paulgoldbaum.influxdbclient

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter}

class HttpClientSuite extends CustomTestSuite with BeforeAndAfter with BeforeAndAfterAll {

  var host = "localhost"
  var port = 64011
  var httpsPort = 64012
  var mockServer: WireMockServer = new WireMockServer(
    wireMockConfig().port(port).containerThreads(5).jettyAcceptors(1).httpsPort(httpsPort)
  )

  before {
    mockServer.start()
    WireMock.configureFor(host, port)
  }

  after {
    mockServer.stop()
  }

  override def afterAll() = {
    mockServer.shutdown()
  }

  test("Basic requests are received") {
    val url = "/query"
    stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(200).withBody("")))
    val client = new HttpClient(host, port)
    val future = client.get(url)
    val result = await(future)
    assert(result.code == 200)
  }

  test("Https requests are received") {
    val url = "/query"
    stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(200).withBody("")))
    val config = new HttpConfig().setAcceptAnyCertificate(true)
    val client = new HttpClient(host, httpsPort, true, null, null, config)
    val future = client.get(url)
    val result = await(future)
    assert(result.code == 200)
  }

  test("Error responses are handled correctly") {
    val url = "/query"
    stubFor(get(urlEqualTo(url))
      .willReturn(
        aResponse()
          .withStatus(500)
          .withBody("")))

    val client = new HttpClient(host, port)
    try {
      await(client.get(url))
      fail("Did not throw exception")
    } catch {
      case e: HttpException =>
        assert(e.code == 500)
    }
  }

  test("Future fails on connection refused") {
    val config = new HttpConfig()
    val client = new HttpClient(host, port - 1, false, null, null, config)

    try {
      await(client.get("/query"))
      fail("Did not throw exception")
    } catch {
      case e: HttpException =>
    }
  }

  test("Future fails if request takes too long") {
    val url = "/query"
    stubFor(get(urlEqualTo(url))
      .willReturn(
        aResponse()
          .withFixedDelay(200)
          .withStatus(200)
          .withBody("")))

    val config = new HttpConfig().setRequestTimeout(50)
    val client = new HttpClient(host, port, false, null, null, config)

    try {
      await(client.get(url))
      fail("Did not throw exception")
    } catch {
      case e: HttpException =>
    }
  }

  test("Future fails if read takes too long") {
    val url = "/query"
    stubFor(get(urlEqualTo(url))
      .willReturn(
        aResponse()
          .withFixedDelay(200)
          .withStatus(200)
          .withBody("")))

    val config = new HttpConfig().setReadTimeout(50)
    val client = new HttpClient(host, port, false, null, null, config)

    try {
      await(client.get(url))
      fail("Did not throw exception")
    } catch {
      case e: HttpException =>
    }
  }

  test("Future fails if the connection takes too long to establish") {
    val url = "/query"

    val config = new HttpConfig().setConnectTimeout(50)
    val client = new HttpClient("192.0.2.1", port, false, null, null, config)

    try {
      await(client.get(url))
      fail("Did not throw exception")
    } catch {
      case e: HttpException =>
    }
  }

  test("Closing a connection more than once throws an exception") {
    val client = new HttpClient(host, port)
    client.close()
    try {
      client.close()
      fail("Did not throw exception")
    } catch {
      case e: HttpException =>
    }
  }

  test("Using a closed connection to send a query returns an exception") {
    val client = new HttpClient(host, port)
    client.close()
    try {
      await(client.get("/query"))
      fail("Did not throw exception")
    } catch {
      case e: HttpException =>
    }
  }
}
