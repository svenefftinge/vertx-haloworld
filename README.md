vertx-haloworld
===============

This is a web-based multi-user chat application using Vert.x (http://vertx.io) together with SockJS. It's not production quality and is only a demonstration of what you can do with these technologies.

Haloworld presents you with a chat interface. In the chat is a chat bot called Hal who can answer some of your questions and can be extended to answer more. Currently he can talk to Wolfram Alpha to answer a small set of questions and he can also spit back some statistics on the JVM heap usage and uptime.

To install simply clone the repository and run the Ant build script. This will create a folder called _dist_ in the base directory. You'll also need to download theVert.x distribution make sure that the _bin_directory is on your path.

To run, execute _bin/haloworld.sh_ in a console. Then browse to http://localhost:8080/
