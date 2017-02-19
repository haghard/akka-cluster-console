package shared

import java.net.InetAddress

object Network {

  def toNum(ip: InetAddress): Int = {
    def read(bytes: scala.Array[Byte]): Int = {
      (bytes(0) << 24) |
        (bytes(1) & 0xff) << 16 |
        (bytes(2) & 0xff) << 8 |
        (bytes(3) & 0xff)
    }
    read(ip.getAddress)
  }

  def toInetAddress(ip: Int) =
    ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF)
}
