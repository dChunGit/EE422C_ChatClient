package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ChatMain extends Application{

	private static BufferedReader reader;
	private static PrintWriter writer;
	private static TextArea input, text;
	private String username;
	
	public static void main(String[] args) {
    	try {
			new ChatMain().run(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(String[] args) throws Exception {
    	launch(args);
	}
	
	private void setUpChat() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("127.0.0.1", 4242);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		ChatMain.reader = new BufferedReader(streamReader);
		ChatMain.writer = new PrintWriter(sock.getOutputStream());

    	ChatMain.writer.println("UserID-" + username + "-EndID");
    	ChatMain.writer.flush();
    	
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
		//System.out.println("Setup finished");
	}
	
	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					//System.out.println(this.toString() + " " + message);
					ChatMain.text.appendText(message + "\n");
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		int big_font = 32;
		
		primaryStage.setTitle("Chat Client");
		
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        
        ScrollPane controls = new ScrollPane();
		VBox container = new VBox();
		container.setPadding(new Insets(10, 20, 10, 10));

		
		Label heading = new Label();
		heading.setText("Project 7 Chat Client");
		heading.setFont(Font.font(big_font));
		
		StackPane header = new StackPane();
		header.getChildren().add(heading);
		StackPane.setAlignment(heading, Pos.CENTER);
		header.setMinWidth(primScreenBounds.getWidth()/3);
		
		//multiple labels dynamically show data left and right sides
		
		
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
		        	String text = input.getText();
		            System.out.println(text);
		            send_message.set_Text(text);
		            
		            Thread message = new Thread(send_message);
		            message.start();
		            
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
	        	String text = input.getText();
	            send_message.set_Text(text);
	            Thread message = new Thread(send_message);
	            message.start();
	            input.clear();
			}
			
		});

		
		BorderPane inputs = new BorderPane();
		inputs.setCenter(input);
		inputs.setRight(send);
		BorderPane.setMargin(send, new Insets(5, 0, 0, 5));
		//StackPane.setAlignment(input, Pos.CENTER_LEFT);
		//StackPane.setAlignment(send, Pos.CENTER_RIGHT);
		//inputs.setMinWidth(primScreenBounds.getWidth()/3);
		inputs.setPadding(new Insets(20, 40, 40, 40));
		
		container.getChildren().add(header);
		container.getChildren().add(text);
		container.getChildren().add(inputs);
		//header.setVisible(false);
		text.setVisible(false);
    	inputs.setVisible(false);
		
		Button visibility = new Button();
		visibility.setText("Back");
		visibility.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				//header.setVisible(!header.isVisible());
				text.setVisible(!text.isVisible());
	        	inputs.setVisible(!inputs.isVisible());
			}
			
		});
		container.getChildren().add(visibility);
		
		StackPane overview = new StackPane();
		overview.getChildren().add(container);
		
		TextField user_name = new TextField();
		TextField password = new TextField();
		Button done_info = new Button();
		done_info.setText("Sign In");
		done_info.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				System.out.println(user_name.getText());
				System.out.println(password.getText());
				controls.setContent(overview);
				primaryStage.sizeToScene();
				if(checkUsername(user_name.getText(), password.getText())) {
					try {
						heading.setText("Welcome: " + user_name.getText());
						setUpChat();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		});
		
		BorderPane sign_in = new BorderPane();
		GridPane authenticate = new GridPane();
		authenticate.add(heading, 0, 0);
		authenticate.add(user_name, 0, 1);
		authenticate.add(password, 0, 2);
		authenticate.add(done_info, 0, 3);
		GridPane.setMargin(heading, new Insets(10, 20, 50, 20));
		GridPane.setMargin(user_name, new Insets(10, 20, 50, 20));
		GridPane.setMargin(password, new Insets(0, 20, 50, 20));
		GridPane.setMargin(done_info, new Insets(0, 20, 50, 20));
		GridPane.setHalignment(done_info, HPos.CENTER);
		BorderPane.setAlignment(authenticate, Pos.CENTER);
		sign_in.setCenter(authenticate);
		
		controls.setContent(sign_in);
		//controls.setContent(container);

		Scene scene1 = new Scene(controls);
		primaryStage.setScene(scene1);
		primaryStage.show();



	}
	
	private boolean checkUsername(String user, String password) {
		//check username exists
		//check password
		username = user;
		return true;
	}
	
	class sendMessage implements Runnable {

	    private String text_field;
	    
	    public void set_Text(String text) {
	    	this.text_field = text;
	    }

	    public void run() {
	    	ChatMain.writer.println(text_field);
	    	ChatMain.writer.flush();
	    }
	}

	

}
