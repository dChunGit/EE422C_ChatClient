package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
	
	
	
	class update_runnable implements Runnable {
		@Override
		public void run() {
			while(true) {
				setChanged();
				notifyObservers("update");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}
	}

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(4242);
		//update_runnable run_update = new update_runnable();
		
		
		/*Thread update_thread = new Thread(run_update);
		update_thread.start();*/
		
		while (true) {
			Socket clientSocket = serverSock.accept();
			String[] info = log_Next(clientSocket);
			String user = info[0];
			String password = info[1];
			ChatRoom_Observer writer = new ChatRoom_Observer(clientSocket.getOutputStream(), user);
			ClientHandler user_detected = new ClientHandler(clientSocket);
			Thread t = new Thread(user_detected);
			t.start();
			this.addObserver(writer);
			System.out.println(user);
			System.out.println(password);
			if(checkUsername(user, password) == 1) {
				//System.out.println("Observer User: "+user);
				System.out.println("User");
				users_active.add(user_detected);
				
				setChanged();
				notifyObservers("update");

				System.out.println("got a connection");
			}else if(checkUsername(user, password) == -2) {
				System.out.println("New User");
				addNewUser(user, password);

				setChanged();
				notifyObservers("update");
				
			}else {
				System.out.println("Failed " + checkUsername(user, password));
				setChanged();
				notifyObservers("failure");
				
			}
			
		}
		
		
	}
	
	private boolean addNewUser(String username, String password) {
		Connection c = null;
	    //Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //System.out.println("Opened database successfully");
	      ArrayList<String> current_users = getNames(getDatabase());
	      if(current_users.contains(username)) {
	    	  return false;
	      }

	      //stmt = c.createStatement();
	      String sql = "INSERT INTO USER_CLIENT (ID,PASSWORD,FRIENDS,CHATS) " +
	                   "VALUES (?, ?, ?, ? );";
	  
	      PreparedStatement ps = c.prepareStatement(sql);
	      ps.setString(1, username);
	      ps.setString(2, password);
	      ps.setString(3, "");
	      ps.setString(4, "");
	      
	      ps.executeUpdate();
	      
	      ps.close();
	      
	      sql = "INSERT INTO USERS (ID,ONLINE) " +
                  "VALUES (?, ? );";
 
	      ps = c.prepareStatement(sql);
	      ps.setString(1, username);
	      ps.setString(2, "online");
	     
	      ps.executeUpdate();
	     
	      ps.close();

	      
	      //stmt.close();
	      c.commit();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    //System.out.println("Records created successfully");
	    return true;
	}
	
	private ArrayList<String> getNames(ArrayList<ArrayList<String>> parse_array) {
		ArrayList<String> names = new ArrayList<String>();
		for(int a = 0; a < parse_array.size(); a++) {
			//System.out.println(parse_array.get(a).toString());
			names.add(parse_array.get(a).get(0));
		}
		
		return names;
	}
	
	private ArrayList<String> getPasswords(ArrayList<ArrayList<String>> parse_array) {
		ArrayList<String> names = new ArrayList<String>();
		for(int a = 0; a < parse_array.size(); a++) {
			//System.out.println(parse_array.get(a).toString());
			names.add(parse_array.get(a).get(1));
		}
		
		return names;
	}
	
	private int checkUsername(String user, String pass) {
		//check username exists
		//check password
		ArrayList<String> online_users = getOnline(true);
		//System.out.println(personal_data.toString());
		//System.out.println(passwords.toString());
		
		if(getNames(getDatabase()).contains(user) && getPasswords(getDatabase()).contains(pass) && !online_users.contains(user) ) {
			return 1;
		}else if(online_users.contains(user)) {
			return -1;
		}else if(!getNames(getDatabase()).contains(user)) {
				return -2;
		}
		//getDatabase();
		return 0;
	}
	
	private ArrayList<String> getOnline(boolean online_check) {
		Connection c = null;
	    Statement stmt = null;
        ArrayList<String> temp = new ArrayList<>();
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //System.out.println("Opened database successfully - existing");
	      

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM USERS;" );
	      while ( rs.next() ) {
	         String name = rs.getString("ID");
	         //System.out.println(name);
	         String online_user = rs.getString("ONLINE");
	         String compare = "online";
	         if(!online_check) {
	        	 compare = "offline";
	         }
	         if(online_user.equals(compare)) {
	        	 temp.add(name);
	         }
	      }
	      rs.close();
	      stmt.close();
	      
	      
	      c.close();
	      
	    } catch ( Exception e ) {
	    	//e.printStackTrace();
	    	System.exit(0);
	    }
	    //System.out.println("Operation done successfully - online users");
	    //System.out.println(temp.toString());
	    return temp;
	}
	
	private ArrayList<ArrayList<String>> getDatabase() {
		Connection c = null;
	    Statement stmt = null;
	    ArrayList<ArrayList<String>> users_data = new ArrayList<>();
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //System.out.println("Opened database successfully - existing");
	      

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM USER_CLIENT;" );
	      while ( rs.next() ) {
	         ArrayList<String> temp = new ArrayList<>();
	         String name = rs.getString("ID");
	         //System.out.println(name);
	         String password = rs.getString("PASSWORD");
	         //System.out.println(password);
	         String friends = rs.getString("FRIENDS");
	         //System.out.println(friends);
	         String chats = rs.getString("CHATS");
	         //System.out.println(chats);
	         
	         
	         temp.add(name);
	         temp.add(password);
	         temp.add(friends);
	         temp.add(chats);
	         users_data.add(temp);
	      }
	      rs.close();
	      stmt.close();
	      
	      
	      c.close();
	      
	    } catch ( Exception e ) {
	    	//e.printStackTrace();
	    	System.exit(0);
	    }
	    //System.out.println("Operation done successfully");
	    return users_data;
	}
	
	private static void setUpDatabase() {
		Connection c = null;
		Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      
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
	      
	      stmt = c.createStatement();
	      sql = "CREATE TABLE IF NOT EXISTS GROUPS " +
	    		"(ID TEXT PRIMARY KEY		NOT NULL," +
	    		" USERS			TEXT	NOT NULL," +
	    		" CHATLOG		TEXT	NOT NULL);";
	      stmt.executeUpdate(sql);
	      stmt.close();
	      
	      c.close();
	      System.out.println("Connection to SQLite has been established.");
	      
	    } catch ( Exception e ) {
	    	e.printStackTrace();
	      //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      //System.exit(0);
	    }
	}
	
	private String[] log_Next(Socket client) {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String[] data = new String[2];
			data[0] = input.readLine();
			data[1] = input.readLine();
	        return data;

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
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			} catch (IOException e) {
				/*setChanged();
				notifyObservers("update");*/
				//e.printStackTrace();
			}
		}
	}
}
