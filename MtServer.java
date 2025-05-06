import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * MtServer.java
 *
 * <p>
 * This program implements a simple multithreaded chat server with game
 * functionality.
 * It keeps an ArrayList of connected clients and allows broadcasting messages.
 * </p>
 */
public class MtServer {
  private ArrayList<Client> clientList; // List of clients
  private ServerSocket serverSock; // Server socket for accepting connections

  public MtServer() {
    clientList = new ArrayList<>();
  }

  private void getConnection() {
    try {
      System.out.println("Waiting for client connections on port 9011.");
      serverSock = new ServerSocket(9011);

      while (true) {
        Socket connectionSock = serverSock.accept(); // Accept incoming connections
        ClientHandler handler = new ClientHandler(connectionSock, clientList);
        Thread clientThread = new Thread(handler);
        clientThread.start(); // Start handling the client in a new thread
      }
    } catch (IOException e) {
      System.err.println("Error during connection handling: " + e.getMessage());
    } finally {
      // Properly close the ServerSocket to avoid resource leaks
      if (serverSock != null && !serverSock.isClosed()) {
        try {
          serverSock.close();
        } catch (IOException e) {
          System.err.println("Error closing server socket: " + e.getMessage());
        }
      }
    }
  }

  public static void main(String[] args) {
    MtServer server = new MtServer();
    server.getConnection();
  }
} // MtServer