package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ChatMain extends Application{

	private static BufferedReader reader;
	private static PrintWriter writer;
	private static Socket socket;
	private static TextArea input, text;
	private static String username, password, group_name = "", chat_user = "";
	private static String ip = "127.0.0.1";
	private static ArrayList<ArrayList<String>> database;
	private static ArrayList<String> personal_data;
	private static ArrayList<String> passwords;
	private static ArrayList<String> chat_with_others, online_users, offline_users;
	private static HBox online_box, offline_box, group_box;
	private static VBox threads_container, container;
	private static Label heading;
	private static Stage primaryStage;
	private static StackPane overview;
	private static ScrollPane controls;
	private static TextField user_name, ipAddress;
	private static PasswordField password_field;
	
	public static void main(String[] args) {
    	try {
			new ChatMain().run(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
    	
    	
	}
	
	public void run(String[] args) throws Exception {
		chat_with_others = new ArrayList<>();
		personal_data = new ArrayList<>();
		passwords = new ArrayList<>();
    	launch(args);
	}
	
	
	
	private void setUpChat(boolean toggle) throws Exception {
		if(toggle) {
			//System.outprintln(ip);
			ChatMain.socket = new Socket("127.0.0.1", 4242);
			InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
			ChatMain.reader = new BufferedReader(streamReader);
			ChatMain.writer = new PrintWriter(socket.getOutputStream());
	    	ChatMain.writer.println(username);
	    	ChatMain.writer.flush();
	    	ChatMain.writer.println(password);
	    	ChatMain.writer.flush();
	    	
	    	
	    	//ChatMain.writer.println("update");
	    	//ChatMain.writer.flush();
	
			if(personal_data == null) {
				////System.outprintln("null");
			}else {
				////System.outprintln(personal_data.toString());
			}
			
			////System.outprintln("networking established");
			Thread readerThread = new Thread(new IncomingReader());
			readerThread.start();
		}else {
			if(ChatMain.socket!=null) {
				ChatMain.writer.println("quit");
				ChatMain.writer.flush();
				//System.outprintln("socket closed on: " + username);
				ChatMain.writer.println("update");
				ChatMain.writer.flush();
				ChatMain.socket.close();
				ChatMain.reader.close();
				ChatMain.writer.close();
			}
		}
		//////System.outprintln("Setup finished");
	}
	
	private class groupUsers implements EventHandler<Event> {
		private String name;
		private ArrayList<String> members;
		
		public groupUsers(ArrayList<String> members) { 
			this.name = members.get(0);
			this.members = parseMembers(members);
			//System.outprintln("Group: " + this.members.toString());
		}
		
		private ArrayList<String> parseMembers(ArrayList<String> parse_array) {
			//System.outprintln(parse_array.toString());
			String[] temp = parse_array.get(1).split(":");
			//System.outprintln(Arrays.toString(temp));
			ArrayList<String> members_parsed = new ArrayList<>();
			
			for(int a = 0; a < temp.length; a++) {
				members_parsed.add(temp[a]);
			}
			
			return members_parsed;
		}
		
		@Override
		public void handle(Event event) {
			group_name = name;
			heading.setText("Chatting with: " + name);
			ChatMain.writer.println("restoregrouplogs");
			ChatMain.writer.flush();
			//restoreGrouplog();
			chat_with_others = members;
			try {
				setUpChat(true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			threads_container.setVisible(false);
			container.setVisible(true);
		}
		
	}
	
	private void updateUsers() {
		ChatMain.writer.println("updateUsers");
		ChatMain.writer.flush();
	}
	
	class IncomingReader implements Runnable {
		public void run() {
			String message;
			//System.outprintln("Read");
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println(this.toString() + " " + message);
					if(message.equals("update")) {
						//setStatus(true);
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								primaryStage.setTitle(username);							
								controls.setContent(overview);
								primaryStage.sizeToScene();
							}
						});
						
					}else if(message.contains("updateUsers")) {
						System.out.println(message);
						database = getDatabase(message.split("-")[1]);
						online_users = getOnline(message.split("-")[2]);
						offline_users = getOnline(message.split("-")[3]);
						System.out.println(offline_users.toString());
						ArrayList<String> users = getNames(database);
						ArrayList<ArrayList<String>> group_check = new ArrayList<ArrayList<String>>();
						try{
							group_check = getDatabase(message.split("-")[4]);
						}catch(Exception e){
							
						}
						//System.outprintln("Group_Check in update: " + group_check.toString());
						Platform.runLater(new Runnable() {
							   @Override
							   public void run() {
								   online_box.getChildren().clear();
								   offline_box.getChildren().clear();
								   group_box.getChildren().clear();
							   }
						});
						
						for(int a = 0; a < group_check.size(); a++) {
							Button group_button = new Button();
							group_button.setText(group_check.get(a).get(0));
						
							
							/*if(group_check.get(a).contains(username)) {
								
							}*/
							groupUsers listener = new groupUsers(group_check.get(a));
							
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									group_box.getChildren().add(group_button);
									group_button.setOnMouseClicked(listener);
								}
							});
						}
						
						for(int a = 0; a < users.size(); a++) {
							/*Label user_icon = new Label(users.get(a).substring(0, 1));

							Text user_icon_name = new Text(users.get(a));
							GridPane user_icon_pane = new GridPane();
							user_icon_pane.add(user_icon, 0, 0);
							user_icon_pane.add(user_icon_name, 0, 1);*/
							Button user_button = new Button();
							user_button.setText(users.get(a));
							//////System.outprintln(username);
							if(online_users.contains(users.get(a)) && !users.get(a).equals(username)) {
								Platform.runLater(new Runnable() {
									   @Override
									   public void run() {
										    online_box.getChildren().add(user_button);
											user_button.setOnMouseClicked(new EventHandler<Event>() {

												@Override
												public void handle(Event event) {
													//////System.outprintln("Clicked");
													heading.setText("Chatting with: " + user_button.getText());
													chat_user = user_button.getText();
													chat_with_others.add(user_button.getText());
													try {
														setUpChat(true);
													} catch (Exception e) {
														// TODO Auto-generated catch block
														//e.printStackTrace();
													}
													if(group_name.equals("")) {
														ChatMain.writer.println("restorelogs");
														ChatMain.writer.flush();
														//restoreChat();
													}
													threads_container.setVisible(false);
													container.setVisible(true);
												}
												
											});
										}
									});
							}else if(offline_users.contains(users.get(a)) && !users.get(a).equals(username)) {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										offline_box.getChildren().add(user_button);
										
									}
								});
							}
						}
					
					}else if(message.contains("signin")) {
						database = getDatabase(message.split("-")[1]);
						//System.outprintln(database);
						ArrayList<String> data = getNames(database);
						////System.outprintln(data.toString());
						personal_data = data;
						passwords = getPasswords(database);
						ip = ipAddress.getText();
						username = user_name.getText();
						password = password_field.getText();
						password_field.setText("");
						

						ChatMain.writer.println("updateUsers");
						ChatMain.writer.flush();
						
					}else if(message.equals("quit")) {
						try {
							setUpChat(false);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}else if(message.contains("restoregrouplogs")) {
						
					
					}else if(message.equals("failure")) {
						try {
							setUpChat(false);
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					}
					else {
						text.setText("");
						//updateChatlog(message);
						String sentfrom[] = message.split(":");
						//System.outprintln(Arrays.toString(sentfrom));
						if(chat_with_others.contains(sentfrom[0]) || sentfrom[0].equals(username)) {
							//System.outprintln("Updating: " + group_name);
							//group_name = "";
							if(!group_name.equals("")) {
								if(sentfrom.length == 3) {
									if(sentfrom[2].equals(group_name)) {
										//System.outprintln("Correct");
										//message = sentfrom[0] + sentfrom[1];

										text.appendText(message);
										//updateGrouplog(message);
										//restoreGrouplog();
									}
								}
							}else {
								if(sentfrom.length < 3) {
									//System.outprintln("Incorrect");
									
									text.appendText(message);
									//updateChatlog(message);
									//restoreChat();
								}
							}
							//ChatMain.text.appendText(message + "\n");
						}
					}
				}
			} catch (IOException ex) {
				//ex.printStackTrace();
			}
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		int big_font = 32;
		ChatMain.primaryStage = stage;
		
		primaryStage.setTitle("Chat Client");
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		Text wrong_info = new Text();

		/*
		 * 
		 * Thread View Interface
		 * 
		 */
		Label online_users = new Label();
		online_users.setText("Who is online");
		Label offline_users = new Label();
		offline_users.setText("Who is offline");
		online_box = new HBox();
		offline_box = new HBox();
		Label groups = new Label();
		groups.setText("Groups");
		group_box = new HBox();
		
		Button new_group = new Button();
		new_group.setText("Start new group chat");
		new_group.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				ScrollPane container_group = new ScrollPane();
				Scene scene2 = new Scene(container_group);
	            Stage create_group = new Stage();
				TextField group_naming = new TextField();
				Button create = new Button();
				MenuButton m = new MenuButton("Select Members");
				ArrayList<String> names = getNames(database);
				for(int a = 0; a <names.size(); a++) {
					m.getItems().add(new CheckMenuItem(names.get(a)));
				}

				
				create.setText("Create Group");
				create.setOnMouseClicked(new EventHandler<Event>() {

					@Override
					public void handle(Event event) {
						ArrayList<String> members = new ArrayList<>();
						for(int a = 0; a < m.getItems().size(); a++) {
							if(((CheckMenuItem) m.getItems().get(a)).isSelected()) {
								members.add(m.getItems().get(a).getText());
							}
						}
						ChatMain.writer.println("addgroup");
						ChatMain.writer.flush();
						ChatMain.writer.println(group_naming.getText());
						ChatMain.writer.flush();
						ChatMain.writer.println(concatMembers(members));
						ChatMain.writer.flush();
						//addGroup(group_naming.getText(), members);
						create_group.close();
						ChatMain.writer.println("update");
						ChatMain.writer.flush();
						
					}
				});
				
				GridPane group_overview = new GridPane();
				group_overview.add(group_naming, 0, 0);
				group_overview.add(create, 1, 0);
				group_overview.add(m, 2, 0);
				
				container_group.setContent(group_overview);
				
	            create_group.setScene(scene2);
	            create_group.show();

			}
			
		});
		
		threads_container = new VBox();
		threads_container.setPadding(new Insets(10, 20, 10, 10));
		
		Label user_signed = new Label();

		threads_container.getChildren().add(online_users);
		threads_container.getChildren().add(online_box);
		threads_container.getChildren().add(offline_users);
		threads_container.getChildren().add(offline_box);
		threads_container.getChildren().add(groups);
		threads_container.getChildren().add(group_box);
		threads_container.getChildren().add(new_group);
		threads_container.getChildren().add(user_signed);
		
        
        /*
         * 
         * Actual Chat Window Interface
         * 
         */
        controls = new ScrollPane();
		container = new VBox();
		BorderPane sign_in = new BorderPane();

		container.setPadding(new Insets(10, 20, 10, 10));

		heading = new Label();
		heading.setText("Project 7 Chat Client");
		heading.setFont(Font.font(big_font));
		
		StackPane header = new StackPane();
		header.getChildren().add(heading);
		StackPane.setAlignment(heading, Pos.CENTER);
		header.setMinWidth(primScreenBounds.getWidth()/3);
		
		text = new TextArea();
		text.setText("");
		text.setEditable(false);
		text.setMinHeight(primScreenBounds.getHeight()/3);
		text.setWrapText(true);
		text.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> ob, String o,
	                String n) {
	            // expand the textfield
	        	text.selectPositionCaret(text.getLength()); 
	    		text.deselect(); 
	        }
	    });
		
		header.getChildren().add(text);
		
		input = new TextArea();
		input.setWrapText(true);
		input.setPrefHeight(12);
		input.setMaxHeight(70);
		input.setPrefWidth(primScreenBounds.getWidth()/6);
		input.setText("");
		input.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> ob, String o,
	                String n) {
	            // expand the textfield
	        	input.setPrefHeight(TextUtils.computeTextHeight(input.getFont(),
	        			input.getText(), input.getPrefWidth()));
	        }
	    });

        sendMessage send_message = new sendMessage();
		input.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent ke) {
		        if (ke.getCode().equals(KeyCode.ENTER)) {
					//System.outprintln("Send");
		        	String text = input.getText();
		            //////System.outprintln(text);
		        	if(!(text.equals("") || text.equals(" ") || text.equals("	"))) {
			            send_message.set_Text(text);
			            //System.outprintln("help " + group_name);
			            /*if(!group_name.equals("")) {
			            	updateGrouplog(text);
			            }*/
			            //updateChatlog(text);
			            Thread message = new Thread(send_message);
			            message.start();
			            
		        	}
		            input.clear();
		        	
		            ke.consume();
		        }
		    }
		});
		
		Button send = new Button();
		send.setText("Send");
		send.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				//System.outprintln("Send");
				String text = input.getText();
	            //////System.outprintln(text);
	        	if(!(text.equals("") || text.equals(" ") || text.equals("	"))) {
		            send_message.set_Text(text);
		            //System.outprintln("help " + group_name);
		            /*if(!group_name.equals("")) {
		            	updateGrouplog(text);
		            }*/
		            //updateChatlog(text);
		            Thread message = new Thread(send_message);
		            message.start();
		            
	        	}
	            input.clear();
			}
			
		});

		BorderPane inputs = new BorderPane();
		inputs.setCenter(input);
		inputs.setRight(send);
		BorderPane.setMargin(send, new Insets(5, 0, 0, 5));
		inputs.setPadding(new Insets(20, 40, 40, 40));
		
		container.getChildren().add(header);
		container.getChildren().add(text);
		container.getChildren().add(inputs);
		/*header.setVisible(false);
		text.setVisible(false);
		inputs.setVisible(false);*/
		container.setVisible(false);
		
		Button visibility = new Button();
		visibility.setText("Sign Out");
		visibility.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				//////System.outprintln(visibility.getText());
				//System.outprintln("Send");
				
				try {
					setUpChat(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				username = "";
				password = "";
				wrong_info.setVisible(false);
				controls.setContent(sign_in);
				primaryStage.sizeToScene();
			}
			
		});
		
		Button back_button = new Button();
		back_button.setText("Back");
		back_button.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				//////System.outprintln(back_button.getText());
				if(back_button.getText().equals("Back")) {
					chat_with_others.clear();
					input.setText("");
					text.setText("");
					group_name = "";
					chat_user = "";
					updateUsers();
					container.setVisible(false);
					threads_container.setVisible(true);

					
				}
			}
			
		});
		
		container.getChildren().add(back_button);
		threads_container.getChildren().add(visibility);
		
		
		
		
		
		/*
		 * 
		 * Set outer stackpane for children
		 * 
		 */
		overview = new StackPane();
		overview.getChildren().add(container);
		overview.getChildren().add(threads_container);
		
	
		
		/*
		 * 
		 * User Sign In Interface
		 * 
		 */
		ipAddress = new TextField();
		ipAddress.setPromptText("Enter IP Address");
		user_name = new TextField();
		user_name.setPromptText("Username");
		password_field = new PasswordField();
		password_field.setPromptText("Password");
		wrong_info.setText("Incorrect Username or Password");
		wrong_info.setVisible(false);
		wrong_info.setFill(Color.RED);

		user_signed.setText(user_name.getText());
		
		Button new_user = new Button();
		new_user.setText("New User");
		new_user.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				
				////System.outprintln(user_name.getText());
				////System.outprintln(password_field.getText());
				
				try {
					ArrayList<String> data = getNames(database);
					////System.outprintln(data.toString());
					personal_data = data;
					passwords = getPasswords(database);
					ip = ipAddress.getText();
					username = user_name.getText();
					password = password_field.getText();
					password_field.setText("");
					
					setUpChat(true);
					/*int status_login = checkUsername(user_name.getText(), password_field.getText());
					if(status_login == 1) {
						password_field.setText("");
						
						setUpChat(true);
						
					}else if(status_login == 0){
						wrong_info.setVisible(true);
						wrong_info.setText("Incorrect Username or Password");
						////System.outprintln("Wrong sign-in");
					}else {
						wrong_info.setVisible(true);
						wrong_info.setText("User already signed in");
					}*/
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				/*try {
					ip = ipAddress.getText();
					username = user_name.getText();
					password = password_field.getText();
					if(addNewUser()) {

						password_field.setText("");
						ArrayList<String> data = getNames(database);
						personal_data = data;
						passwords = getPasswords(database);
						if(personal_data == null) {
							////System.outprintln("null");
						}else {
							////System.outprintln(personal_data.toString());
						}
						
						heading.setText("Welcome: " + data.get(0));
						setUpChat(true);
						primaryStage.setTitle(username);

						//updateUsers();
						
						setStatus(true);
						controls.setContent(overview);
						primaryStage.sizeToScene();
					}else {
						wrong_info.setText("Username already exists");
						////System.outprintln("Duplicate Users");
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}*/
				
			}
			
		});
		
		Button done_info = new Button();
		done_info.setText("Sign In");
		done_info.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				////System.outprintln(user_name.getText());
				////System.outprintln(password_field.getText());
				
				try {
					username = user_name.getText();
					password = password_field.getText();
					setUpChat(true);
					//ChatMain.writer.println("signin");
					//ChatMain.writer.flush();
					
					/*int status_login = checkUsername(user_name.getText(), password_field.getText());
					if(status_login == 1) {
						password_field.setText("");
						setUpChat(true);
						
					}else if(status_login == 0){
						wrong_info.setVisible(true);
						wrong_info.setText("Incorrect Username or Password");
						////System.outprintln("Wrong sign-in");
					}else {
						wrong_info.setVisible(true);
						wrong_info.setText("User already signed in");
					}*/
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			
		});
		
		Label welcome = new Label();
		welcome.setText("Project 7 Chat Client");
		welcome.setFont(Font.font(big_font));
		
		StackPane welcome_stack = new StackPane();
		welcome_stack.getChildren().add(welcome);
		StackPane.setAlignment(welcome, Pos.CENTER);
		
		GridPane options = new GridPane();
		options.add(done_info, 0, 0);
		options.add(new_user, 1, 0);
		GridPane.setMargin(done_info, new Insets(10, 10, 10, 10));
		GridPane.setMargin(new_user, new Insets(10, 10, 10, 10));
		options.setAlignment(Pos.CENTER);
		
		GridPane authenticate = new GridPane();
		authenticate.add(welcome_stack, 0, 0);
		authenticate.add(ipAddress, 0, 1);
		authenticate.add(user_name, 0, 2);
		authenticate.add(password_field, 0, 3);
		authenticate.add(wrong_info, 0, 4);
		authenticate.add(options, 0, 5);
		GridPane.setMargin(welcome_stack, new Insets(10, 20, 50, 20));
		GridPane.setMargin(ipAddress, new Insets(10, 20, 50, 20));
		GridPane.setMargin(user_name, new Insets(0, 20, 0, 20));
		GridPane.setMargin(password_field, new Insets(0, 20, 0, 20));
		GridPane.setMargin(wrong_info, new Insets(0, 20, 50, 20));
		GridPane.setMargin(options, new Insets(0, 20, 50, 20));
		GridPane.setHalignment(options, HPos.CENTER);
		GridPane.setHalignment(wrong_info, HPos.CENTER);
		BorderPane.setAlignment(authenticate, Pos.CENTER);
		sign_in.setCenter(authenticate);
		
		controls.setContent(sign_in);
		
		
		/*
		 * 
		 * Set Scene
		 * 
		 */
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				
				try {
					setUpChat(false);
				} catch (Exception f) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		});
		
		Scene scene1 = new Scene(controls);
		primaryStage.setScene(scene1);
		primaryStage.show();



	}
	
	private ArrayList<String> getOnline(String message) {
       ArrayList<String> temp = new ArrayList<>();
	   online_users = parseOnline(message);
	   
	   return temp;
	}
	
	private ArrayList<String> parseOnline(String message) {
		ArrayList<String> data_base = new ArrayList<>();
		String[] online_users = message.split(":");
		for(int a = 0; a < online_users.length; a++) {
			data_base.add(online_users[0]);
		}
		
		return data_base;
	}
	
	private ArrayList<String> getPasswords(ArrayList<ArrayList<String>> parse_array) {
		ArrayList<String> names = new ArrayList<String>();
		for(int a = 0; a < parse_array.size(); a++) {
			////System.outprintln(parse_array.get(a).toString());
			names.add(parse_array.get(a).get(1));
		}
		
		return names;
	}
	
	private ArrayList<String> getNames(ArrayList<ArrayList<String>> parse_array) {
		ArrayList<String> names = new ArrayList<String>();
		for(int a = 0; a < parse_array.size(); a++) {
			////System.outprintln(parse_array.get(a).toString());
			names.add(parse_array.get(a).get(0));
		}
		
		return names;
	}
	
	private ArrayList<ArrayList<String>> getDatabase(String data) {
		ArrayList<ArrayList<String>> data_base = new ArrayList<>();
		String[] users_split = data.split(":");
		for(int a = 0; a < users_split.length; a++) {
			String[] data_split = users_split[a].split("\\.");
			ArrayList<String> temp = new ArrayList<>();
			for(int b = 0; b < data_split.length; b++) {
				temp.add(data_split[b]);
			}
			data_base.add(temp);
		}
		
		return data_base;
	}
	
	
	private String concatMembers(ArrayList<String> con) {
		String message = "";
		for(int a = 0; a < con.size(); a++) {
			message += con.get(a) + ":";
		}
		
		return message;
	}
	
	
	
	class sendMessage implements Runnable {

	    private String text_field;
	    
	    public void set_Text(String text) {
	    	this.text_field = text;
	    }

	    public void run() {
	    	//System.outprintln("Sending message: " + text_field);
	    	String message = username+":";
			for(int a = 0; a < chat_with_others.size(); a++) {
				message += "BEGCHAT" + chat_with_others.get(a) + "ENDCHAT";
			}
			message += ":" + this.text_field;
			message += ":" + group_name;
	    	ChatMain.writer.println(message);
	    	ChatMain.writer.flush();
	    }
	}

	

}
