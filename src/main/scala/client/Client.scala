package client

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{Socket, InetAddress}
import player.Player

/*
The client opens a socket, sends its player's name to the server, and hands off control to 
a ProxyReferee to continue the game.
*/
class Client(address: String, port: Int, player: Player) extends Runnable {

  def run(): Unit = {
    val socket = new Socket(InetAddress.getByName(address), port)
    sendMessage(socket, player.name)

    val proxyReferee = new ProxyReferee(socket, player)
    proxyReferee.run
  }

  private def sendMessage(socket: Socket, message: String): Unit = {
    val out = new PrintWriter(socket.getOutputStream, true)
    out.println(message) // sends newline character
  }
}