package com.paulgoldbaum.influxdbclient

import com.ning.http.client.AsyncHttpClientConfig

class HttpConfig {
  private var builder = new AsyncHttpClientConfig.Builder

  def setConnectTimeout(timeout: Int) = {
    builder = builder.setConnectTimeout(timeout)
    this
  }

  def setReadTimeout(timeout: Int) = {
    builder = builder.setReadTimeout(timeout)
    this
  }

  def setRequestTimeout(timeout: Int) = {
    builder = builder.setRequestTimeout(timeout)
    this
  }

  def setAcceptAnyCertificate(acceptAnyCertificate: Boolean) = {
    builder = builder.setAcceptAnyCertificate(acceptAnyCertificate)
    this
  }

  protected[influxdbclient] def build() = builder.build()
}

