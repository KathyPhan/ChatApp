package WholesomeChat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;


// For every client's connection we call this class
public class clientThread extends Thread{
  private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private MultiThreadChatServerSync mtcss;

  public clientThread(Socket clientSocket, clientThread[] threads, MultiThreadChatServerSync mtcss) {
    this.clientSocket = clientSocket;
    this.mtcss = mtcss;
  }

  public void run() {
    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      while (true) {
        os.println("Enter your name.");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("The name should not contain '@' character.");
        }
      }

      /* Welcome the new the client. */
      os.println("Welcome " + name
          + " to our chat room.\nTo leave enter /quit in a new line.");
      
      mtcss.chatJoin(name, this);
      clientName = "@" + name;
      
      /* Start the conversation. */
      while (true) {
        String line = is.readLine();
        if (line.startsWith("/quit")) {
          break;
        }
        /* If the message is private sent it to the given client. */
        if (line.startsWith("@")) {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              mtcss.privateMessage(name, words, this);
              os.println(">" + name + "> " + words[1]);
            }
          }
        } else {
        	/* The message is public, broadcast it to all other clients. */
        	mtcss.publicMessage(name, line, this);
          }
      }
      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      mtcss.leaveRoom(name, this);
      os.println("*** Bye " + name + " ***");

      
      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
  
  public PrintStream getOs(){
	  return os;
  }
  
  public String getClientName()
  {
	  return clientName;
  }
}