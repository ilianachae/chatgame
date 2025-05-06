import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * ClientListener.java
 *
 * <p>This class runs on the client end and just
 * displays any text received from the server.
 *
 */

public class ClientListener implements Runnable {
  private Socket connectionSock = null;

  ClientListener(Socket sock) {
    this.connectionSock = sock;
  }

  /**
   * Gets message from server and dsiplays it to the user.
   */
  public void run() {
    try (BufferedReader serverInput = new BufferedReader(
            new InputStreamReader(connectionSock.getInputStream()))) {

      String serverText;
      while ((serverText = serverInput.readLine()) != null) {
        System.out.println(serverText);
      }
      // Connection was lost
      System.out.println("Closing connection for socket " + connectionSock);
      // If readLine() returns null, server closed the connection
      System.out.println("Server has closed the connection. Program shutting down...");
      if (!connectionSock.isClosed()) {
        connectionSock.close();
      }
      System.exit(0);  // Optional: exit the whole client program

    } catch (IOException e) {
      if (!connectionSock.isClosed()) {
        System.out.println("Connection error: " + e.getMessage());
      }
    }
  }
} // ClientListener for MtClient