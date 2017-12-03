package WholesomeChat;

import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

// the Server class
public class MultiThreadChatServerSync {
   // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;

  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int maxClientsCount = 10;
  private static final clientThread[] threads = new clientThread[maxClientsCount];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 2222;
    if (args.length < 1) {
      System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Open a server socket on the portNumber (default 2222). Note that we can
     * not choose a port less than 1023 if we are not privileged users (root).
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    MultiThreadChatServerSync mtcss = new MultiThreadChatServerSync();
    mtcss.syncThreads();
  }  
  
  public void syncThreads()
  {
	  while (true) {
	      try {
	        clientSocket = serverSocket.accept();
	        int i = 0;
	        for (i = 0; i < maxClientsCount; i++) {
	          if (threads[i] == null) {
	            (threads[i] = new clientThread(clientSocket, threads, this)).start();
	            break;
	          }
	        }
	        if (i == maxClientsCount) {
	          PrintStream os = new PrintStream(clientSocket.getOutputStream());
	          os.println("Server too busy. Try later.");
	          os.close();
	          clientSocket.close();
	        }
	        
	      } catch (IOException e) {
	        e.printStackTrace();;
	      }
	    }
  }
  
  public void chatJoin(String name, clientThread currThread)
  {
	  for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != currThread) {
            threads[i].getOs().println("*** A new user " + name
                + " entered the chat room !!! ***");
          }
        }
  }
  
  public void privateMessage(String name, String[] words, clientThread currThread)
  {
	  for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != currThread
              && threads[i].getClientName() != null
              && threads[i].getClientName().equals(words[0])) {
            threads[i].getOs().println("<" + name + "> " + words[1]);
            /*
             * Echo this message to let the client know the private
             * message was sent.
             */
            break;
          }
        }
  }
  
  public void publicMessage(String name, String line, clientThread currThread)
  {
	  for (int i = 0; i < maxClientsCount; i++) {
		  if (threads[i] != null && threads[i].getClientName() != null) {
			  threads[i].getOs().println("<" + name + "> " + line);
		  }
	  }
  }
  
  public void leaveRoom(String name, clientThread currThread)
  {
	  for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != currThread
              && threads[i].getClientName() != null) {
            threads[i].getOs().println("*** The user " + name
                + " is leaving the chat room !!! ***");
        }
      }

      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] == currThread) {
          threads[i] = null;
        }
      }
  }
}