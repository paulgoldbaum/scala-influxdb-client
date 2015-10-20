package com.paulgoldbaum.influxdbclient

import java.net.{InetSocketAddress, DatagramPacket, DatagramSocket}

class UdpClient protected[influxdbclient](host: String, port: Int) {

  val socket = new DatagramSocket()
  val address = new InetSocketAddress(host, port)

  def write(point: Point) = {
    val payload = point.serialize().getBytes
    val packet = new DatagramPacket(payload, payload.length, address)
    socket.send(packet)
  }

}
