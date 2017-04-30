package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;


public class ServerMain extends Observable {
	
	int i = 0;
	
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
		////System.outprintln(InetAddress.getLocalHost().getHostAddress());
		//update_runnable run_update = new update_runnable();
		
		
		/*Thread update_thread = new Thread(run_update);
		update_thread.start();*/
		
		while (true) {
			Socket clientSocket = serverSock.accept();
			//System.out.println("test");
			String[] info = log_Next(clientSocket);
			//System.out.println("test");
			String user = info[0];
			String password = info[1];
			ChatRoom_Observer writer = new ChatRoom_Observer(clientSocket.getOutputStream(), user);
			ClientHandler user_detected = new ClientHandler(clientSocket, user);
			Thread t = new Thread(user_detected);
			t.start();
			this.addObserver(writer);
			////System.outprintln(user);
			//System.out.println("Check user: " + checkUsername(user, password));
			if(checkUsername(user, password) == 1) {
				//////System.outprintln("Observer User: "+user);
				////System.outprintln("User");
				users_active.add(user_detected);
				setStatus(true, user);
				setChanged();
				notifyObservers("update");
				
				String sending_message = "signin" + "-" + parseDatabase();
				////System.outprintln(sending_message);
				setChanged();
				notifyObservers(sending_message);

				////System.outprintln("got a connection");
			}else if(checkUsername(user, password) == -2) {
				////System.outprintln("New User");
				addNewUser(user, password);
				setStatus(true, user);

				setChanged();
				notifyObservers("update");
				
			}else {
				////System.outprintln("Failed " + checkUsername(user, password));
				setChanged();
				notifyObservers("failure");
				
			}
			
		}
		
		
	}
	
	private void setStatus(boolean status, String username) {
		//System.out.println(i++ + " " + status + username);
		
		Connection c = null;
	    //Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //////System.outprintln("Opened database successfully");

	      String sql = "UPDATE USERS set ONLINE = ? where ID=?;";

	      PreparedStatement ps = c.prepareStatement(sql);
	      ps = c.prepareStatement(sql);
	      if(!status) {
	    	  ps.setString(1, "offline");
	      }else {
	    	  ps.setString(1, "online");
	      }
	      ps.setString(2, username);
	     
	      ps.executeUpdate();
	     
	      ps.close();

	      
	      //stmt.close();
	      c.commit();
	      c.close();
	    } catch ( Exception e ) {
	      ////System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      ////System.exit(0);
	    }
	    //////System.outprintln("Records created successfully");
	}
	
	private boolean addNewUser(String username, String password) {
		Connection c = null;
	    //Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //////System.outprintln("Opened database successfully");
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
	      //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      //System.exit(0);
	    }
	    //////System.outprintln("Records created successfully");
	    return true;
	}
	
	private ArrayList<String> getNames(ArrayList<ArrayList<String>> parse_array) {
		ArrayList<String> names = new ArrayList<String>();
		for(int a = 0; a < parse_array.size(); a++) {
			//////System.outprintln(parse_array.get(a).toString());
			names.add(parse_array.get(a).get(0));
		}
		
		return names;
	}
	
	private ArrayList<String> getPasswords(ArrayList<ArrayList<String>> parse_array) {
		ArrayList<String> names = new ArrayList<String>();
		for(int a = 0; a < parse_array.size(); a++) {
			//////System.outprintln(parse_array.get(a).toString());
			names.add(parse_array.get(a).get(1));
		}
		
		return names;
	}
	
	private int checkUsername(String user, String pass) {
		//check username exists
		//check password
		ArrayList<String> online_users = getOnline(true);
		//////System.outprintln(personal_data.toString());
		//////System.outprintln(passwords.toString());
		//System.out.println("Online: " + online_users.toString());
		
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
	
	private String parseOnline(boolean check) {
		ArrayList<String> users_data = getOnline(check);
		String concat = "";
		for(int a = 0; a < users_data.size(); a++) {
			concat += users_data.get(a) + ":";
		}
		
		return concat;
		
	}
	
	private ArrayList<String> getOnline(boolean online_check) {
		Connection c = null;
	    Statement stmt = null;
        ArrayList<String> temp = new ArrayList<>();
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //////System.outprintln("Opened database successfully - existing");
	      

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM USERS;" );
	      while ( rs.next() ) {
	         String name = rs.getString("ID");
	         //////System.outprintln(name);
	         String online_user = rs.getString("ONLINE");
	         //System.out.println(name + " " +online_user);
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
	    	//System.exit(0);
	    }
	    //////System.outprintln("Operation done successfully - online users");
	    //////System.outprintln(temp.toString());
	    return temp;
	}
	
	private void restoreGrouplog(String username, String group_name) {
		////System.outprintln("Restoring Group");
		ArrayList<String> chatlogs = retrieveGroupHistory(username, group_name);
		////System.outprintln("Group history: " + chatlogs.toString());
		String temp = "";
		for(int a = 0; a < chatlogs.size(); a++) {
			temp += chatlogs.get(a) + "\n";
			
			//text.appendText(chatlogs.get(a) + "\n");
			//////System.outprintln(chatlogs.get(a));
		}
		setChanged();
		notifyObservers(temp);
	}
	
	private void updateGrouplog(String text, String group_name, ArrayList<String> chat_with_others) {
		////System.outprintln("Grouplog: " + text);
		Connection c = null;
	    //Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //////System.outprintln("Opened database successfully");
	      ArrayList<String> database = getNames(getGroups());

	      String sql = "UPDATE GROUPS set CHATLOG = ? where ID=?;";

	      PreparedStatement ps = c.prepareStatement(sql);
	      ps = c.prepareStatement(sql);
	      
	      int a = 0;
	      boolean found = false;
	      while(a < database.size() && !found) {
	    	  if(database.get(a).equals(group_name)) {
	    		  found = true;
	    	  }else {
	    		  a++;
	    	  }
	      }
	      if(found) {
		      ArrayList<String> chatlog = getGroups().get(a);
		      String chats = chatlog.get(2);
		      for(int b = 0; b < chat_with_others.size(); b++) {
					chats += "USERID."+chat_with_others.get(b)+"."+text+"-";
			  }
		      ////System.outprintln("Group Chats saved: "+chats);
		      
	    	  ps.setString(1, chats);
		      ps.setString(2, group_name);
		     
		      ps.executeUpdate();
	      }
	     
	      ps.close();

	      
	      //stmt.close();
	      c.commit();
	      c.close();
	    } catch ( Exception e ) {
	    	e.printStackTrace();
	      //System.exit(0);
	    }
	}
	
	private ArrayList<String> retrieveGroupHistory(String username, String group_name) {
		////System.outprintln("Retrieving group chat");
		ArrayList<String> database = getNames(getGroups());
	    ArrayList<String> chat_logs = new ArrayList<>();
		int a = 0;
		boolean found = false;
		while(a < database.size() && !found) {
	    	 if(database.get(a).equals(group_name)) {
	    		 found = true;
	    	 }else {
		    	 a++;
	    	 }
	    }
        ArrayList<String> chatlog = getGroups().get(a);
        String chats = chatlog.get(2);
    	////System.outprintln("Group Chats: " + chats);
	    String[] threads = chats.split("-");
    	//////System.outprintln("Group Thread array: " + Arrays.toString(threads));
	    for(int b = 0; b < threads.length; b++) {
		    String messages = "";
	    	String temp = threads[b];
	    	//////System.outprintln("Parsed string: " + temp);
	    	String[] parser = temp.split("\\.");
	    	////System.outprintln(Arrays.toString(parser));
	    	if(parser.length > 1) {
	    		
	    		if(parser.length > 2 && parser[1].equals(username)) {
	    			String parse_message = parser[2].split(":")[0];
	    			if(parse_message.equals(parser[1])) {
	    				messages = parser[2];
	    			}else {
	    				messages = parser[2];
	    			}
	    		}
	    	}
	    	//////System.outprintln("Parsed array: " + Arrays.toString(parser));
		    chat_logs.add(messages);
	    }
	    
	    return chat_logs;
	}
	
	private void updateChatlog(String text, String username, ArrayList<String> chat_with_others) {
		Connection c = null;
	    //Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //////System.outprintln("Opened database successfully");
	      ArrayList<String> database = getNames(getDatabase());

	      String sql = "UPDATE USER_CLIENT set CHATS = ? where ID=?;";

	      
	      int a = 0;
	      boolean found = false;
	      while(a < database.size() && !found) {
	    	  if(database.get(a).equals(username)) {
	    		  found = true;
	    	  }else {
	    		  a++;
	    	  }
	      }
	      if(found) {
		      ArrayList<String> chatlog = getDatabase().get(a);
		      String chats = chatlog.get(3);
		      
		      for(int b = 0; b < chat_with_others.size(); b++) {
		    	  //String temp = username + ":" + text.split(":")[1];
		    	  chats += "USERID."+chat_with_others.get(b)+"."+text+"-";
			  }

		      ////System.outprintln("Chat logs: " +chats);

		      PreparedStatement ps = c.prepareStatement(sql);
		      ps = c.prepareStatement(sql);
		      
	    	  ps.setString(1, chats);
		      ps.setString(2, username);
		     
		      ps.executeUpdate();
		      ps.close();
		      
		      for(int b = 0; b < chat_with_others.size(); b++) {

			      ////System.outprintln("Chat logs: " +chats);

			      ps = c.prepareStatement(sql);
			      ps = c.prepareStatement(sql);
			      
		    	  ps.setString(1, chats);
			      ps.setString(2, chat_with_others.get(b));
			     
			      ps.executeUpdate();
			      ps.close();
		      }
	      }
	     

	      
	      //stmt.close();
	      c.commit();
	      c.close();
	    } catch ( Exception e ) {
	    	e.printStackTrace();
	      //System.exit(0);
	    }
	    //////System.outprintln("Records created successfully");
	}
	
	private ArrayList<String> parse(String arg) {
		String message = arg;
		ArrayList<String> temp = new ArrayList<>();
		int a = 0;
		while(a + 6 < message.length()) {
			String checkbeing = message.substring(a, a + 7);
			////System.outprintln("Checkbeing: " + checkbeing);
			if(checkbeing.equals("BEGCHAT")) {
				int ending = a;
				String endcheck = checkbeing;
				while(!endcheck.equals("ENDCHAT") && (ending+7) < message.length()) {
					////System.outprintln("Endcheck: " + endcheck);
					ending++;
					endcheck = message.substring(ending, ending + 7);
				}
				temp.add(message.substring(a + 7, ending));
			}
			a++;
		}
		
		return temp;
	}
	
	private void restoreChat(String username, String chat_user) {
		System.out.println("test");
		ArrayList<String> chatlogs = retrieveHistory(username, chat_user);
		System.out.println(chatlogs.toString());
		String temp = "";
		for(int a = 0; a < chatlogs.size(); a++) {
			temp += chatlogs.get(a) + "\n";
		}
		System.out.println(temp);
		setChanged();
		notifyObservers(temp);
		
	}
	
	private ArrayList<String> retrieveHistory(String username, String chat_user) {
		ArrayList<String> database = getNames(getDatabase());
	    ArrayList<String> chat_logs = new ArrayList<>();
		int a = 0;
		boolean found = false;
		//////System.outprintln(username);
		while(a < database.size() && !found) {
	    	 if(database.get(a).equals(username)) {
	    		 found = true;
	    	 }else {
		    	 a++;
	    	 }
	    }
		if(found) {
	        ArrayList<String> chatlog = getDatabase().get(a);
	        System.out.println("Retrieve: " + chatlog.toString());
	        String chats = chatlog.get(3);
	    	System.out.println("Chats: " + chats);
		    String[] threads = chats.split("-");
	    	//////System.outprintln("Thread array: " + Arrays.toString(threads));
		    for(int b = 0; b < threads.length; b++) {
			    String messages = "";
		    	String temp = threads[b];
		    	//////System.outprintln("Parsed string: " + temp);
		    	String[] parser = temp.split("\\.");
		    	if(parser.length > 1 ) {
		    		
		    		if(parser.length > 2 && (parser[1].equals(chat_user) || parser[1].equals(username))) {
		    			messages = parser[2];
		    		}
		    	}
		    	//////System.outprintln("Parsed array: " + Arrays.toString(parser));
			    chat_logs.add(messages);
		    }
		}
	    
	    return chat_logs;
	}
	
	private String concatMembers(ArrayList<String> con) {
		String message = "";
		for(int a = 0; a < con.size(); a++) {
			message += con.get(a) + ":";
		}
		
		return message;
	}
	
	private boolean addGroup(String name, ArrayList<String> members) {
		Connection c = null;
	    //Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //////System.outprintln("Opened database successfully");
	      ArrayList<String> current_users = getNames(getGroups());
	      if(current_users.contains(name)) {
	    	  return false;
	      }

	      //stmt = c.createStatement();
	      String sql = "INSERT INTO GROUPS (ID,USERS,CHATLOG) " +
	                   "VALUES (?, ?, ?);";
	  
	      PreparedStatement ps = c.prepareStatement(sql);
	      ps.setString(1, name);
	      String concat_members = concatMembers(members);
	      ps.setString(2, concat_members);
	      ps.setString(3, "");
	      
	      ps.executeUpdate();
	      
	      ps.close();

	      
	      //stmt.close();
	      c.commit();
	      c.close();
	    } catch ( Exception e ) {
	      //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      //System.exit(0);
	    }
	    //////System.outprintln("Records created successfully");
	    return true;
	}
	
	private ArrayList<ArrayList<String>> getGroups() {
		Connection c = null;
	    Statement stmt = null;
	    ArrayList<ArrayList<String>> groups_data = new ArrayList<>();
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //////System.outprintln("Opened database successfully - existing");
	      

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM GROUPS;" );
	      while(rs.next()) {
	          ArrayList<String> temp = new ArrayList<>();
	    	  String name = rs.getString("ID");
		      //////System.outprintln(name);
	    	  String users = rs.getString("USERS");
		      //////System.outprintln(users);
	    	  String chatlog = rs.getString("CHATLOG");
		      //////System.outprintln(users);
	    	  temp.add(name);
	    	  temp.add(users);
	    	  temp.add(chatlog);
	    	  groups_data.add(temp);
	      } 
	      
	      rs.close();
	      stmt.close();
	      
	      
	      c.close();
	      
	    }catch (Exception e){
	    	
	    }
	    return groups_data;
	}
	
	private String parseDatabase() {
		ArrayList<ArrayList<String>> users_data = getDatabase();
		String[] temp = new String[users_data.size()];
		for(int a = 0; a < users_data.size(); a++) {
			ArrayList<String> user = users_data.get(a);
			String temp_data = "";
			for(int b = 0; b < user.size(); b++) {
				temp_data += user.get(b) + ".";
			}
			temp[a] = temp_data;
		}
		String concat = "";
		for(int a = 0; a < temp.length; a++) {
			concat += temp[a] + ":";
		}
		
		return concat;
		
	}
	
	private String parseGroups() {
		ArrayList<ArrayList<String>> users_data = getGroups();
		String[] temp = new String[users_data.size()];
		for(int a = 0; a < users_data.size(); a++) {
			ArrayList<String> user = users_data.get(a);
			String temp_data = "";
			for(int b = 0; b < user.size(); b++) {
				temp_data += user.get(b) + ".";
			}
			temp[a] = temp_data;
		}
		String concat = "";
		for(int a = 0; a < temp.length; a++) {
			concat += temp[a] + ":";
		}
		
		return concat;
		
	}
	
	private ArrayList<ArrayList<String>> getDatabase() {
		Connection c = null;
	    Statement stmt = null;
	    ArrayList<ArrayList<String>> users_data = new ArrayList<>();
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:users.db");
	      c.setAutoCommit(false);
	      //////System.outprintln("Opened database successfully - existing");
	      

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM USER_CLIENT;" );
	      while ( rs.next() ) {
	         ArrayList<String> temp = new ArrayList<>();
	         String name = rs.getString("ID");
	         //////System.outprintln(name);
	         String password = rs.getString("PASSWORD");
	         //////System.outprintln(password);
	         String friends = rs.getString("FRIENDS");
	         //////System.outprintln(friends);
	         String chats = rs.getString("CHATS");
	         System.out.println("Chats: " + chats);
	         
	         
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
	    	//System.exit(0);
	    }
	    //////System.outprintln("Operation done successfully");
	    System.out.println(users_data.toString());
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
	      ////System.outprintln("Connection to SQLite has been established.");
	      
	    } catch ( Exception e ) {
	    	e.printStackTrace();
	      ////System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      ////System.exit(0);
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
		private String name;

		public ClientHandler(Socket clientSocket, String username) {
			Socket sock = clientSocket;
			name = username;
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
					System.out.println("server read "+message);
					if(message.contains("quit")) {
						String readin = message.split(":")[1];
						setStatus(false, readin);
						setChanged();
						notifyObservers(message);
					}else if(message.equals("signin")) {
						String sending_message = message + "-" + parseDatabase();
						////System.outprintln(sending_message);
						setChanged();
						notifyObservers(sending_message);
					}else if(message.equals("updateUsers")) { 
						String sending_message = message + "-" + parseDatabase() + 
								"-" + parseOnline(true) + "-" + parseOnline(false) + 
								"-" + parseGroups();
						setChanged();
						notifyObservers(sending_message);
					
					}else if(message.contains("restoreLogs")) {
						System.out.println("right");
						restoreChat(name, message.split(":")[1]);
						
					}else {
						System.out.println(message);
						updateChatlog(message.split(":")[2], name, parse(message.split(":")[1]));
						setChanged();
						notifyObservers(message);
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
