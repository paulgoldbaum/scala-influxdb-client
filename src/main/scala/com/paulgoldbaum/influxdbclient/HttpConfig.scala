package com.paulgoldbaum.influxdbclient

import org.asynchttpclient.DefaultAsyncHttpClientConfig

class HttpConfig {
  private var builder = new DefaultAsyncHttpClientConfig.Builder

  def setConnectTimeout(timeout: Int) = {
    builder = builder.setConnectTimeout(timeout)
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

