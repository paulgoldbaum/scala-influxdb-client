package com.paulgoldbaum.influxdbclient

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._

import com.paulgoldbaum.influxdbclient.HttpClient.Config
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class HttpClientSuite extends FunSuite with BeforeAndAfterEach {

  var host = "localhost"
  var port = 64011
  var mockServer: WireMockServer = new WireMockServer(wireMockConfig().port(port))

  override def beforeEach() = {
    mockServer.start()
    WireMock.configureFor(host, port)
  }

  override def afterEach() = {
    mockServer.stop()
  }

  test("Basic requests are received") {
    val url = "/query"
    stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(200).withBody("")))
    val client = new HttpClient(host, port)
    val future = client.get(url)
    val result = Await.result(future, 5.seconds)
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
    val future = client.get(url)
    try {
      Await.result(future, 5.seconds)
      fail("Did not throw exception")
    } catch {
      case e: HttpException =>
        assert(e.getMessage.contains("500"))
    }
  }

  ignore("Future fails if server not available") {
    val config = new Config().setRequestTimeout(20).setReadTimeout(20).setConnectTimeout(20)
    val client = new HttpClient(host, port, null, null, config)

    val future = client.get("/query", Map("q" -> "SHOW DATABASES"))
    Await.result(future, 5.seconds)
  }

  test("Future fails if response takes too long") {
    val url = "/query"
    stubFor(get(urlEqualTo(url))
      .willReturn(
        aResponse()
          .withFixedDelay(200)
          .withStatus(200)
          .withBody("")))

    val config = new Config().setRequestTimeout(50)
    val client = new HttpClient(host, port, null, null, config)

    val future = client.get(url)
    try {
      Await.result(future, 5.seconds)
      fail("Did not throw exception")
    } catch {
      case e: HttpException =>
    }
  }
}
