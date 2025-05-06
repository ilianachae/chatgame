import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * ClientHandler.java
 * This class handles communication between clients and the server. It manages
 * game logic, including sending questions, awarding points, and displaying
 * scores.
 */
public class ClientHandler implements Runnable {
  private Socket connectionSock;
  private ArrayList<Client> clientList;
  private Client newClient;
  private boolean disconnected = false;
  private boolean isHost = false;

  public ClientHandler(Socket sock, ArrayList<Client> clientList) {
    this.connectionSock = sock;
    this.clientList = clientList;
  }

  @Override
  public void run() {
    try {
      BufferedReader clientInput = new BufferedReader(
          new InputStreamReader(connectionSock.getInputStream()));
      DataOutputStream clientOutput = new DataOutputStream(connectionSock.getOutputStream());

      assignUsername(clientInput, clientOutput);
      sendWelcomeMessage(clientOutput);

      while (connectionSock.isConnected() && !disconnected) {
        String clientText = clientInput.readLine();

        if (clientText != null) {
          processClientMessage(clientText, clientOutput);
        } else {
          handleDisconnect();
        }
      }
    } catch (IOException e) {
      System.err.println("Error: " + e.toString());
      handleDisconnect();
    }
  }

  private void assignUsername(BufferedReader clientInput, DataOutputStream clientOutput)
      throws IOException {
    if (clientList.isEmpty()) {
      newClient = new Client(connectionSock, "HOST");
      isHost = true;
      String hostInstructions = "You are the host!\n"
              + "---------------------------------------------------------\n"
              + "Available commands and syntax:\n"
              + "---------------------------------------------------------\n"
              + "  Send a multiple-choice question: \n" 
              + "      ASK MC <question>;<optionA>;<optionB>;<optionC>;<optionD>\n"
              + "  Send a true/false question:      ASK TF <question>\n"
              + "  Give score manually:             SCORE <username> <points>\n"
              + "  Public scoreboard:               SCORES\n"
              + "  List all connected users:        WHO?\n"
              + "  Direct message in secretly:      DM <username> <message>\n"
              + "---------------------------------------------------------\n";
      clientOutput.writeBytes(hostInstructions);
    } else {
      clientOutput.writeBytes("Please enter your unique username:\n");
      String username = clientInput.readLine().trim();

      while (isUsernameTaken(username)) {
        clientOutput.writeBytes("Username is taken, try another one:\n");
        username = clientInput.readLine().trim();
      }

      newClient = new Client(connectionSock, username);
      // Notify the client that their username is accepted
      clientOutput.writeBytes("Username '" + username + "' is accepted.\n");

      String userInstructions = "Welcome, " + username + "!\n"
              + "---------------------------------------------------------\n"
              + "Available commands:\n"
              + "---------------------------------------------------------\n"
              + "  WHO?:                       List all connected users\n"
              + "  DM <username> <message>:    Direct message in secretly\n"
              + "  GOODBYE:                    Disconnect from the server\n"
              + "---------------------------------------------------------\n";
      clientOutput.writeBytes(userInstructions);
    }

    clientList.add(newClient);
    broadcast(newClient.username + " has joined the chat.");
  }

  private boolean isUsernameTaken(String username) {
    return clientList.stream().anyMatch(client -> client.username.equals(username));
  }

  private void sendWelcomeMessage(DataOutputStream clientOutput) throws IOException {
    clientOutput.writeBytes("Welcome, " + newClient.username + "!\n");
  }

  private void processClientMessage(
      String clientText, DataOutputStream clientOutput) throws IOException {
    if (clientText != null) {
      if (clientText.equalsIgnoreCase("Goodbye")) {
        handleGoodbye();
        return; // Stop further processing, ensure clean exit
      } else if (clientText.equalsIgnoreCase("Who?")) {
        sendListOfUsers(clientOutput);
      } else if (isHost && clientText.startsWith("ASK ")) {
        handleQuestion(clientText, clientOutput);
      } else if (isHost && clientText.startsWith("SCORE ")) {
        handleScoreCommand(clientText, clientOutput);
      } else if (isHost && clientText.equalsIgnoreCase("SCORES")) {
        sendScores(clientOutput);
      } else if (clientText.toUpperCase().startsWith("DM ")) {
        handleDirectMessage(clientText.substring(3).trim());
      } else {
        handleMessage(clientText);
      }
    } else {
      handleDisconnect();
    }
  }

  private void handleMessage(String message) throws IOException {
    String formattedMessage = newClient.username + ": " + message; // Proper formatting
    broadcast(formattedMessage); // Broadcast to all clients
  }

  private void handleDirectMessage(String text) throws IOException {
    int firstSpaceIndex = text.indexOf(' ');
    if (firstSpaceIndex == -1) {
      return;
    }

    String username = text.substring(0, firstSpaceIndex);
    String message = text.substring(firstSpaceIndex + 1);

    Client recipient = findClientByUsername(username);
    if (recipient != null) {
      DataOutputStream recipientOutput =
          new DataOutputStream(recipient.connectionSock.getOutputStream());
      recipientOutput.writeBytes("DM from '" + newClient.username + "': " + message + "\n");
    } else {
      DataOutputStream senderOutput = new DataOutputStream(connectionSock.getOutputStream());
      senderOutput.writeBytes("User '" + username + "' not found.\n");
    }
  }

  private void sendListOfUsers(DataOutputStream clientOutput) throws IOException {
    StringBuilder userList = 
        new StringBuilder("-------------------------\nCurrent users:\n-------------------------\n");
    for (Client client : clientList) {
      userList.append(client.username).append("\n");
    }
    userList.append("-------------------------\n");
    clientOutput.writeBytes(userList.toString()); // Send to the client
  }

  private void handleQuestion(String clientText, DataOutputStream clientOutput) {
    String[] parts = clientText.split(" ", 3);
    if (parts.length < 3) {
      try {
        clientOutput.writeBytes("Error: Question command is incomplete.\n");
      } catch (IOException e) {
        System.err.println("Error sending error message to host: " + e.getMessage());
      }
      return;
    }

    String type = parts[1];  // MC or TF
    String content = parts[2]; // The rest of the text including the question and options

    String formattedQuestion;
    if (type.equalsIgnoreCase("MC")) {
      formattedQuestion = formatMultipleChoiceQuestion(content);
    } else if (type.equalsIgnoreCase("TF")) {
      formattedQuestion = formatTrueFalseQuestion(content);
    } else {
      try {
        clientOutput.writeBytes("Error: Invalid question type."
            + " Use 'MC' for multiple-choice or 'TF' for true/false.\n");
      } catch (IOException e) {
        System.err.println("Error sending error message to host: " + e.getMessage());
      }
      return;
    }

    // Send formatted question to the host only
    try {
      clientOutput.writeBytes("Question screen (preview):\n" + formattedQuestion + "\n");
    } catch (IOException e) {
      System.err.println("Error sending question preview to host: " + e.getMessage());
    }

    // Broadcast the question to all clients
    broadcast(formattedQuestion);
  }

  private String formatMultipleChoiceQuestion(String content) {
    String[] parts = content.split(";");
    if (parts.length < 5) {
      return "Error: Incomplete multiple-choice question."
        + "Ensure you provide 1 question and 4 options.";
    }

    String question = parts[0];
    String optionA = parts[1].trim();
    String optionB = parts[2].trim();
    String optionC = parts[3].trim();
    String optionD = parts[4].trim();

    return "-------------------------\n"
          + "Question (MC): " + question + "\n"
          + "A) " + optionA + "\n"
          + "B) " + optionB + "\n"
          + "C) " + optionC + "\n"
          + "D) " + optionD + "\n"
          + "-------------------------\n";
  }

  private String formatTrueFalseQuestion(String content) {
    return "-------------------------\n"
          + "Question (TF): " + content + "\n"
          + "A) True\n"
          + "B) False\n"
          + "-------------------------\n";
  }

  private void handleScoreCommand(String clientText, DataOutputStream clientOutput)
      throws IOException {
    String[] parts = clientText.split(" "); // Corrected split
    if (parts.length == 3) {
      String username = parts[1];
      int points;
      try {
        points = Integer.parseInt(parts[2]); // Ensure valid points
      } catch (NumberFormatException e) {
        clientOutput.writeBytes("Invalid points value. Use an integer.\n");
        return;
      }

      Client targetClient = findClientByUsername(username);
      if (targetClient != null) {
        targetClient.score += points; // Correct handling of score assignment
        broadcast(username + " received " + points + " points.");
      } else {
        clientOutput.writeBytes("Client '" + username + "' not found.\n");
      }
    } else {
      clientOutput.writeBytes("Invalid command format. Use: SCORE <username> <points>.\n");
    }
  }

  private Client findClientByUsername(String username) {
    return clientList.stream()
      .filter(client -> client.username.equals(username)).findFirst().orElse(null);
  }

  private void sendScores(DataOutputStream clientOutput) throws IOException {
    StringBuilder scores = new 
        StringBuilder("-------------------------\nCurrent scores:\n-------------------------\n");
    // clientList.forEach(client -> scores.append(client.username + ": " + client.score + "\n"));

    // Filter out the host when displaying scores
    for (Client client : clientList) {
      if (!client.username.equals("host")) {
        scores.append(client.username).append(": ").append(client.score).append("\n");
      }
    }
    scores.append("-------------------------\n");
    broadcast(scores.toString()); // Send scores to all clients
  }

  private void handleGoodbye() throws IOException {
    broadcast(newClient.username + " has left the chat.");
    clientList.remove(newClient);
    if (!connectionSock.isClosed()) {
      connectionSock.close(); // Corrected check for socket closure
    }
    disconnected = true;
  }

  private void handleDisconnect() {
    if (!disconnected && !connectionSock.isClosed()) {
      try {
        connectionSock.close(); // Corrected closure handling
      } catch (IOException e) {
        System.err.println("Error during disconnection: " + e.getMessage());
      }
      broadcast(newClient.username + " has left the chat.");
      clientList.remove(newClient);
      disconnected = true;
    }
  }

  private void broadcast(String message) {
    clientList.forEach(client -> {
      if (client.connectionSock != connectionSock) {
        try {
          DataOutputStream clientOutput = 
              new DataOutputStream(client.connectionSock.getOutputStream());
          clientOutput.writeBytes(message + "\n");
        } catch (IOException e) {
          System.err.println("Error broadcasting: " + e.getMessage());
        }
      }
    });
  }
  
} // ClientHandler for MtClient