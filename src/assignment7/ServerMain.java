package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Observable;


public class ServerMain extends Observable {
	
	private static ArrayList<ClientHandler> users_active;

	public static void main(String[] args) {
		users_active = new ArrayList<>();
		setUpDatabase();
		try {
			new ServerMain().setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setUpDatabase() {
		Connection c = null;
		Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      //System.out.println("Connection to SQLite has been established.");
	      
	      stmt = c.createStatement();
	      String sql = "CREATE TABLE IF NOT EXISTS USER_CLIENT " +
	                   "(ID TEXT PRIMARY KEY     NOT NULL," +
	                   " PASSWORD           TEXT    NOT NULL, " + 
	                   " FRIENDS            TEXT     NOT NULL, " + 
	                   " CHATS        TEXT     NOT NULL);"; 
	      stmt.executeUpdate(sql);
	      stmt.close();
	      
	      
	      stmt = c.createStatement();
	      sql = "CREATE TABLE IF NOT EXISTS USERS " +
	    		"(ID TEXT PRIMARY KEY		NOT NULL," +
	    		" ONLINE			TEXT	NOT NULL);";
	      stmt.executeUpdate(sql);
	      stmt.close();
	      
	      c.close();
	      
	    } catch ( Exception e ) {
	    	e.printStackTrace();
	      //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      //System.exit(0);
	    }
	}

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(4242);
		while (true) {
			Socket clientSocket = serverSock.accept();
			String user = log_user(clientSocket);
			//System.out.println("Observer User: "+user);
			ChatRoom_Observer writer = new ChatRoom_Observer(clientSocket.getOutputStream(), user);
			ClientHandler user_detected = new ClientHandler(clientSocket);
			users_active.add(user_detected);
			Thread t = new Thread(user_detected);
			t.start();
			this.addObserver(writer);
			setChanged();
			notifyObservers("update");

			//System.out.println("got a connection");
		}
	}
	
	private String log_user(Socket client) {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
	        return input.readLine();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	class ClientHandler implements Runnable {
		private BufferedReader reader;

		public ClientHandler(Socket clientSocket) {
			Socket sock = clientSocket;
			try {
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					//System.out.println("server read "+message);
					setChanged();
					notifyObservers(message);
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
