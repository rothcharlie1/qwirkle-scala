# The Client

The Client is a Runnable that accepts a server address and port and a Player instance, connects to a game, and instantiates a ProxyReferee to play the game once connected.

# The ProxyReferee

The ProxyReferee accepts a Socket and a Player, listens to incoming server messages on the Socket, and wraps its Player's methods with JSON input and output to the server.

```
+-------------------------+     +------------------------+
| Client extends Runnable |     | ProxyReferee           |
+-------------------------+     +------------------------+
| address: String         |     | socket: Socket         |
| port: Int               |     | player: Player         |
| player: Player          |     +------------------------+
+-------------------------+  |->| run: Unit              |
| override run(): Unit    |--|  +------------------------+
+-------------------------+
```

### How the Proxy Referee Works

The ProxyReferee has a private function playerMethodFactory: JsValue -> (JsValue -> JsValue) which allows the ProxyReferee to delegate incoming JSON to Player method calls. The return value of playerMethodFactory is a private method that wraps an individual Player method with JSON deserialization and serialization. This design relies on the fact that incoming method calls are structured as a 2-element array, where the first element indicates the method and the second provides the arguments.

The basic process of the ProxyReferee as pseudocode:
```
loop {
    msg <- nextJson()
    writeJSON(playerMethodFactory(msg[0])(msg[1]))
}
```

# The ClientConfig

The ClientConfig is a configuration for a group of clients. It accepts a list of Players and server connection parameters and is used by xclients to spawn a group of Clients. 


