import java.net.Socket;

public class Client {
  public Socket connectionSock = null;
  public String username = "";
  public int score;

  public Client(Socket connectionSock, String username) {
    this.connectionSock = connectionSock;
    this.username = username;
    this.score = 0; // Initialize score to zero
  }
} // Client.java