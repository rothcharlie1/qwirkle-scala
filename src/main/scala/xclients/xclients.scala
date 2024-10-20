package xclients

import common.QSerializer
import client._

object XClients {
    
    def main(args: Array[String]): Unit = {
        val cfg = ClientConfig.fromJClientConfig(QSerializer.getNextJson.get)
        val port = args.lift(0) match {
            case Some(value) => value.toInt
            case None => throw new Exception("No argument provided for port")
        }

        for (player <- cfg.players) {
            Thread.sleep(cfg.waiting * 1000)
            new Thread(new Client(cfg.host, port, player)).start()
        }
        
    }
}