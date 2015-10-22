package com.paulgoldbaum.influxdbclient

import java.net.{InetSocketAddress, DatagramPacket, DatagramSocket}

class UdpClient protected[influxdbclient](host: String, port: Int) {

  val socket = new DatagramSocket()
  val address = new InetSocketAddress(host, port)

  def write(point: Point) = {
    send(point.serialize().getBytes)
  }

  def write(points: List[Point]) = {
    send(points.map(_.serialize()).mkString("\n").getBytes)
  }

  private def send(payload: Array[Byte]) = {
    val packet = new DatagramPacket(payload, payload.length, address)
    socket.send(packet)
  }
}
