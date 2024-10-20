package comms

import java.net.Socket
import scala.io.BufferedSource
import scala.concurrent.duration.FiniteDuration

/*
Provides common utility functions that clients and servers may want to use.
*/
object CommunicationUtils {

    /*
    Attempts to get the next string available on 'socket' within period 'timeout'.
    */
    def receiveNext(socket: Socket, timeout: FiniteDuration): Option[String] = {
        val in = new BufferedSource(socket.getInputStream).getLines()
        val endTime = System.currentTimeMillis() + timeout.toMillis
        while (System.currentTimeMillis() < endTime + 50) {
            if (in.hasNext) return Some(in.next())
            Thread.sleep(50) // Sleep to avoid busy waiting
        }
        None
    }
}