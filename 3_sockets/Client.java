import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

  // Notification
  private String notif = " *** ";

  // Input and output streams
  private ObjectInputStream sInput;
  private ObjectOutputStream sOutput;

  // Socket
  private Socket socket;

  // Server, username and port
  private String server, username;
  private int port;

  // Constructor
  Client(String server, int port, String username) {
    this.server = server;
    this.port = port;
    this.username = username;
  }

  // Start client
  public boolean start() {

    // Try connecting to server
    try {
      socket = new Socket(server, port);
    } catch (Exception ec) {
      display("Error connecting to server: " + ec);
      return false;
    }

    String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();

    display(msg);

    // Create streams
    try {
      sInput = new ObjectInputStream(socket.getInputStream());
      sOutput = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException eIO) {
      display("Exception creating streams: " + eIO);
      return false;
    }

    // Thread to listen from server
    new ListenFromServer().start();

    // Send username
    try {
      sOutput.writeObject(username);
    } catch (IOException eIO) {
      display("Exception during login: " + eIO);
      disconnect();
      return false;
    }

    return true;
  }

  // Display message
  private void display(String msg) {
    System.out.println(msg);
  }

  // Send message
  void sendMessage(ChatMessage msg) {
    try {
      sOutput.writeObject(msg);
    } catch (IOException e) {
      display("Exception writing to server: " + e);
    }
  }

  // Disconnect
  private void disconnect() {

    try {
      if (sInput != null) sInput.close();
    } catch (Exception e) {
    }

    try {
      if (sOutput != null) sOutput.close();
    } catch (Exception e) {
    }

    try {
      if (socket != null) socket.close();
    } catch (Exception e) {
    }
  }

  // Main
  public static void main(String[] args) {

    int portNumber = 1500;
    String serverAddress = "localhost";
    String userName = "Anonymous";

    Scanner scan = new Scanner(System.in);

    System.out.println("Enter username:");
    userName = scan.nextLine();

    // Create client
    Client client = new Client(serverAddress, portNumber, userName);

    // Start client
    if (!client.start()) return;

    System.out.println("\nWelcome to the chat room.");
    System.out.println("Instructions:");
    System.out.println("1. Type message for broadcast");
    System.out.println("2. Type '@username message' for private message");
    System.out.println("3. Type 'WHOISIN' to see users");
    System.out.println("4. Type 'LOGOUT' to exit");

    while (true) {

      System.out.print("> ");

      String msg = scan.nextLine();

      // Logout
      if (msg.equalsIgnoreCase("LOGOUT")) {
        client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
        break;
      }

      // WHOISIN
      else if (msg.equalsIgnoreCase("WHOISIN")) {
        client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
      }

      // Normal message
      else {
        client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
      }
    }

    scan.close();

    client.disconnect();
  }

  // Thread listening from server
  class ListenFromServer extends Thread {

    public void run() {

      while (true) {

        try {

          String msg = (String) sInput.readObject();

          System.out.println(msg);

          System.out.print("> ");
        } catch (IOException e) {
          display(notif + "Server closed connection: " + e + notif);
          break;
        } catch (ClassNotFoundException e2) {
        }
      }
    }
  }
}

