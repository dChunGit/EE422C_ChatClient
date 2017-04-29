package assignment7;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public class ChatRoom_Observer extends PrintWriter implements Observer {
	private String user_observer;
	
	public ChatRoom_Observer(OutputStream out, String user) {
		super(out);
		this.user_observer = user;
		System.out.println(this + " " + user_observer);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		String[] parsed_expression = ((String) arg).split(":");
		String sending_name = parsed_expression[0];
		System.out.println(Arrays.toString(parsed_expression));
		String users_chat;
		ArrayList<String> update_users = new ArrayList<>();
		
		if(parsed_expression.length > 1) {
			users_chat = parsed_expression[1];
			update_users = parse(users_chat);
		}
		//System.out.println(sending_name);
		
		
		if(update_users.contains(user_observer) || sending_name.equals("update") || sending_name.equals(user_observer)) {
			String sending = "";
			if(parsed_expression.length > 2) {
				sending = sending_name + ": " + parsed_expression[2];
				if(parsed_expression.length > 3) {
					sending += ":" + parsed_expression[3];
				}
				//System.out.println(sending);
				this.println(sending);
				this.flush();
			}else {
				System.out.println(arg);
				this.println(arg); //writer.println(arg);
				this.flush(); //writer.flush();
			}
		}
	}
	
	private ArrayList<String> parse(String arg) {
		String message = arg;
		ArrayList<String> temp = new ArrayList<>();
		int a = 0;
		while(a + 6 < message.length()) {
			String checkbeing = message.substring(a, a + 7);
			//System.out.println("Checkbeing: " + checkbeing);
			if(checkbeing.equals("BEGCHAT")) {
				int ending = a;
				String endcheck = checkbeing;
				while(!endcheck.equals("ENDCHAT") && (ending+7) < message.length()) {
					//System.out.println("Endcheck: " + endcheck);
					ending++;
					endcheck = message.substring(ending, ending + 7);
				}
				temp.add(message.substring(a + 7, ending));
			}
			a++;
		}
		
		return temp;
	}

}