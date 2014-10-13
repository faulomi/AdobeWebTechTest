README

ARCHITECTURE

This file purpose is to explain how I've chosen to implement a multi-thread web server with thread-pooling.

Basically, I decided to build this HTTP Server on top of the the Asynchronous NIO framework introduced in the JDK7, and 
benefit of the ProActor pattern advantages.

REFERENCES
https://tools.ietf.org/html/rfc7230
https://tools.ietf.org/html/rfc7231
https://webtide.com/on-jdk-7-asynchronous-io/
http://openjdk.java.net/projects/nio/presentations/TS-4222.pdf


PROJECT PROPERTIES

The source code was written with Intellij IDEA 13, using a maven 3 project layout.


HOW TO RUN THE PROGRAM

Prerequisites

- JDK 1.8.11+
- Maven 3.2.3 (and executable in the environment path)

There are two options :


1) Open a terminal and go the project root folder.
1) Build the project running the following command : mvn package
2) Go the target subdirectory and run the following command : java -jar adobe-web-tech-test-1.0.jar

Please not that you can pass the following command-line parameters to the program:

-port <port> : HTTP server listening port (default : 8080)
-useSSL : Is the server running on SSL ? (self-signed certificate) (default : false)
-rootPath : root path for the static files (default : current working directory)

You can find the log files in the logs directory.

The second way to execute the program is to directly use the following command :

mvn exec:exec

But in this case, no logs will be output to the console so I recommend the first solution.


LIMITS & IMPROVEMENTS


- HTTP chunking not fully implemented (that means that the file size limit is 2GB as I'm using memory mapped files)
- Unit testing (using rest-assured for example : https://code.google.com/p/rest-assured/)
- Improve documentation but you still can generate it using : mvn javadoc:javadoc
- Improve licensing notes



Happy review !
Jerome,fingers crossed