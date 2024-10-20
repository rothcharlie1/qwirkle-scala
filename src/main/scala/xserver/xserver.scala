package xserver

import server.Server
import server.ServerConfig
import common.QSerializer

/*
Runs a Server with the configuration provided on STDIN.
*/
object XServer {
    
    def main(args: Array[String]): Unit = {
        var config = ServerConfig.fromJServerConfig(QSerializer.getNextJson.get)
        val port = args.lift(0) match {
            case None => throw new Exception("No port provided on STDIN.")
            case Some(value) => value.toInt
        }
        config = config.copy(port = port)
        println(new Server(config).start.toJGameResult.compactPrint)
    }
}