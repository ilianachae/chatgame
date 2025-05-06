# chatgame

This repo contains programs to implement a multi-threaded TCP chat server and client

* MtClient.java handles keyboard input from the user.
* ClientListener.java receives responses from the server and displays them
* MtServer.java listens for client connections and creates a ClientHandler for each new client
* ClientHandler.java receives messages from a client and relays it to the other clients.

Using these file, developed a trivia chatgame that able to connect the server and client, send/receive the messages and recognize each clients.

### Features

* The server will not permit the client to choose a username that is already being used by another client
  - If a client tries to use a name that is already being used, the server will prompt the client to try another name
  - Notify the client when their username is accepted with welcome message
* Display a list of possible commands to the client when their username is accepted
  + When a client types "Who?":
    - the server will respond by sending that client a list of all the usernames of the other clients who are currently connected
  + When a client types "Goodbye":
    - the client will exit the chat gracefully (i.e. without any unhandled error messages)
    - the client will be removed from the server's ArrayList
    - the other clients will be notified that it has left the chat
    - the client program will exit
  + When a client types "DM <username> <message>", they can send a message directly and secretly
* The first client to connect will be given the username "host" and will become the host of the game
  - Tell the host how to ask questions and assign points, and how to use the commands
  - When the host types "SCORES", a message with the scores of each client will be sent to all the clients
  - The host also can use all the commands that the client can use
* The host will send a multiple-choice or true/false question to the other clients
  - the host could award points manually to the clients with typing "SCORE"
* The Client object will be modified to contain a 'score', which will be updated when a client is awarded points by the host

## Identifying Information

* Name: Iliana Chae
* Student ID: 2409413
* Email: ychae@chapman.edu

<br />

* Name: Hunter Peasley
* Student ID: 2390162
* Email: hpeasley@chapman.edu

<br />

* Name: Jordan Silver
* Student ID: 2352988
* Email: jorsilver@chapman.edu

<br />

* Name: Kyle Tran-Vu
* Student ID: 2380099
* Email: tranvu@chapman.edu

<br />

* Course: CPSC 353-02
* Assignment: PA04 Chatgame

## Source Files

* Client.java
* ClientHandler.java
* ClientListener.java
* MtClient.java
* MtServer.java

## References

* N/A

## Known Errors

* N/A

## Build Insructions

  ```sh
  javac *.java
  ```

## Execution Instructions

  ```sh
  java MtServer
  java MtClient
  ```

## Contributions

* Iliana
  - tested client-side input
  - tested client-server connection
  - reviewed the code
  - update readme
  - fix the error after the user left with goodbye command:
    - generates the infinte loop of null 
      -  no more null
    - does not terminate even the client left the chat
      - the client will exit the chat gracefully without any error messages
  - adjusted the host's "SCORES" command to check only clients except host
  - adjusted the function that the host will send multiple choice or true/false question to the other clients
  - adjusted the function that notify the client when their username is accepted
  - adjusted the function that display a list of possible commands to the client when their username is accepted
  - added the extra command function, "DM" that the clients can send a message directly to another client/host secretly

<br />

* Hunter
  - changed port number on MtServer.java
  - added join message with correct Client username
  - added Client username with message broadcast
  - tested the program with multiple clients
  - added functions to check the other clients with "Who?" message
  - added exit function with "Goodbye" message
  - added the function that first client to connect will be given the username "host" and will become the host of the game
  - added the function that tell the host how to ask questions and assign points, and how to use the SCORES command
  - added the function that the host will send any question to the other clients
  - added the score function that clients is awarded/updated points by the host
  - added the host's "SCORES" command to print the current scores of each client to the all the clients include host

<br />

* Jordan
  - Updated README.md
  - Tested the program 

<br />

* Kyle
  - tested client-side input
  - tested client-server connection