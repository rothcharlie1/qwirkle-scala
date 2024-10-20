package server

import player.Player
import common.{MapMap, Tile}
import common.TurnInfo
import player.Action
import common.Tile
import comms.CommunicationUtils

import spray.json._
import java.net.Socket
import scala.io.BufferedSource
import java.io.PrintStream
import scala.concurrent.duration._

/*
The ProxyPlayer accepts a socket, a name, and a call timeout and enables communication via method-call
to a single remote player.
*/
class ProxyPlayer(socket: Socket, nameStr: String, commTimeout: Int) extends Player {
    private val out = new PrintStream(socket.getOutputStream)

    override def name: String = nameStr

    override def setup(info: TurnInfo, st: List[Tile]): Unit = {
        val jpub = info.toJPub
        val tileJson = JsArray(st.map(_.toJTile))
        val argument = JsArray(jpub :: tileJson :: Nil)
        val message = buildMessage("setup", argument)
        sendAndReceive(message) match {
            case JsString("void") => 
            case _ => throw new Exception("Received response other than void.")
        }
    }

    override def takeTurn(info: TurnInfo): Action = {
        val jpub = info.toJPub
        val message = buildMessage("take-turn", JsArray(jpub :: Nil))
        Action.fromJChoice(sendAndReceive(message))
    }

    override def newTiles(tiles: List[Tile]): Unit = {
        val jtiles = JsArray(tiles.map(_.toJTile))
        val message = buildMessage("new-tiles", jtiles)
        sendAndReceive(message) match {
            case JsString("void") => 
            case _ => throw new Exception("Received response other than void.")
        }
    }

    override def win(w: Boolean): Unit = {
        val message = buildMessage("win", JsArray(JsBoolean(w) :: Nil))
        sendAndReceive(message) match {
            case JsString("void") => 
            case _ => throw new Exception("Received response other than void.")
        }
    }

    /*
    Constructs a standard server message with a method name and the necessary arguments.
    */
    private def buildMessage(name: String, args: JsArray): JsArray = {
        JsArray(JsString(name) :: args :: Nil)
    }

    /*
    Sends the provided JSON to the client and returns their response as JSON.
    Throws an exception if the client does not respond in time.
    */
    private def sendAndReceive(json: JsValue): JsValue = {
        out.println(json.compactPrint)

        CommunicationUtils.receiveNext(socket, commTimeout.seconds) match {
            case Some(str) => str.parseJson
            case None => throw new Exception("Did not receive response in time.")
        }
    }

}