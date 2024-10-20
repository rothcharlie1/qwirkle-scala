# The Server

The Server is a class that takes a ServerConfig and provides a method ```start``` to play a game according to its config. It generates a set of ProxyPlayers and then passes them to the Referee to play a game.

# The ProxyPlayer

The ProxyPlayer is an instance of Player that communicates with remote players. It is instantiated with a Socket, a name String, and an Int representing the number of seconds it ought to wait for its player to respond to requests.

```
+-------------------+             +----------------------------+
| Server            |         |-->| ProxyPlayer extends Player |
+-------------------+ (inits) |   +----------------------------+
| start: GameResult |---------|   | socket: Socket             |
+-------------------+             | nameStr: String            |
                                  | commTimeout: Int           |
                                  +----------------------------+
```