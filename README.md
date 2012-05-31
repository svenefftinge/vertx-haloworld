Haloworld
===============

This is a web-based multi-user chat application using [Vert.x](http://vertx.io) together with
[SockJS](https://github.com/sockjs/sockjs-client).
It's not production ready and is only a demonstration of what you can do with these technologies.

Haloworld presents you with a chat interface. In the chat is a chat bot called Hal who can answer some of your questions
and can be extended to answer more. Currently he can talk to Wolfram Alpha to answer a small set of questions and he can
also spit back some statistics on the JVM heap usage and uptime.

Installation
---------------

To install Haloworld, do the following:

* Clone the vertx-haloworld repository
* You'll also need to download the Vert.x [distribution](http://vertx.io/downloads.html)
  and make sure that the _bin_ directory is on your path. Haloworld was developed and tested against Vert.x 1.0-final.
  Note that Vert.x needs JDK 1.7.0 or later to run.
* If you want to try out the Wolfram Alpha integration, you will need to get yourself a Wolfra API key. Then browse
  to src/mods/wolfram-alpha and change the constant field _API_KEY_ in class _com.shinetech.mods.wolframalpha.WolframAlphaVerticle_
  (sorry for the hard coding - will fix this later!).
* Run the Ant build script. The build script expects you to pass in the path to your Vert.x installation as a property. E.g.:
  _ant -DVERTX_HOME=~/Software/vert.x-1.0.final_. The build script will create a folder called _dist_ in the current directory.


To run, execute _bin/haloworld.sh_ in a console. Then browse to [http://localhost:8080/](http://localhost:8080/).

Using the demo
---------------

When you first load the demo you'll be prompted to enter a chat name to login. Once you do that the main page will
appear and you'll see a chat window in the right hand side of the page. If everything is running properly, within a
second or two you should see a message from the chat bot Hal welcoming you to the chat. If you login from multiple
browser sessions you can use the app as a simple multi-user chat.

The left side of the page is used for displaying answers to questions that you ask Hal. To ask a question, simply prefix
it with Hal's chat handle @hal.

Questions Hal can answer
------------------------

Some examples are:

* @hal what time is it?
* @hal when did Albert Einstein live?
* @hal show me the server stats
* @hal what is the population of Australia?

If you have any questions, feel free to email me at
[simon.collins@shinetech.com](mailto:simon.collins@shinetech.com?subject=Question about vertx-haloworld demo).
