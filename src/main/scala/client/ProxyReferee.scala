package client

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{Socket, InetAddress}
import player.Player
import common.Tile
import spray.json._
import java.util.stream.Collectors
import common.TurnInfo

/*
A ProxyReferee enables communication between a player and a remote server. It accepts incoming JSON, 
calls the relevant methods on its Player, and sends the result back to the server as JSON.
*/
class ProxyReferee(socket: Socket, player: Player) {

    def run: Unit = {
        try {
            val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

            while (true) {
                val receivedMessage = in.lines().collect(Collectors.joining());
                val jsonResponse = receivedMessage.parseJson match {
                    case JsArray(elements) if elements.size == 2 => playerMethodFactory(elements(0))(elements(1))
                    case _ => throw new IllegalArgumentException("Received bogus message from server.")
                }
                sendMessage(socket, jsonResponse.compactPrint)
            }
        } catch {
            case e: Exception =>
                e.printStackTrace()
        } finally {
            socket.close()
        }
    }

    /*
    Determines which Player method to call given the incoming JSON MName.
    */
    private def playerMethodFactory(json: JsValue): JsValue=>JsValue = json match {
        case JsString("setup") => wrappedSetup
        case JsString("take-turn") => wrappedTakeTurn
        case JsString("new-tiles") => wrappedNewTiles
        case JsString("win") => wrappedWin
        case _ => throw new IllegalArgumentException("Received bogus message from server.") 
    }

    /*
    The below 'wrapped' methods deserialize the expected data, call the specified function,
    and return the result as serialized JSON.
    */

    private def wrappedSetup(json: JsValue): JsValue = json match {
        case JsArray(elements) if elements.size == 2 => {
            val info = TurnInfo.fromJPub(elements(0))
            val tiles = elements(1) match {
                case JsArray(jtiles) => jtiles.map(Tile.fromJTile(_)).toList
                case _ => throw new IllegalArgumentException("Second argument to setup was not a list of tiles.")
            }
            player.setup(info, tiles)
            JsString("void")
        } 
        case _ => throw new IllegalArgumentException("Setup was not passed the correct arguments")
    }

    def wrappedTakeTurn(json: JsValue): JsValue = json match {
        case JsArray(elements) if elements.size == 1 => {
            val info = TurnInfo.fromJPub(elements(0))
            player.takeTurn(info).toJChoice
        }
        case _ => throw new IllegalArgumentException("Incorrect info provided to take-turn.")
    }

    def wrappedNewTiles(json: JsValue): JsValue = json match {
        case JsArray(elements) if elements.size == 1 => elements(0) match {
            case JsArray(jtiles) => {
                val tiles = jtiles.map(Tile.fromJTile(_)).toList
                player.newTiles(tiles)
                JsString("void")
            }
            case _ => throw new IllegalArgumentException("First argument to new-tiles was not an array.")
        }
        case _ => throw new IllegalArgumentException("new-tiles was passed something other than a single-element array.")
    }

    def wrappedWin(json: JsValue): JsValue = json match {
        case JsArray(elements) if elements.size == 1 => elements(0) match {
            case JsBoolean(bool) => {
                player.win(bool)
                JsString("void")
            }
            case _ => throw new IllegalArgumentException("First argument was not a boolean.")
        } 
        case _ => throw new IllegalArgumentException("Win arguments was not an array.")
    }

    /*
    Sends a string message to the server via the provided socket.  
    */
    def sendMessage(socket: Socket, message: String): Unit = {
        val out = new PrintWriter(socket.getOutputStream, true)
        out.println(message)
    }
}
