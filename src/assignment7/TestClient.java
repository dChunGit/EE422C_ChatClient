package assignment7;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class TestClient extends Application{
	private BufferedReader reader;
	private PrintWriter writer;
	private TextArea input, text;
	
	public void run(String[] args) throws Exception {
		launch(args);
		setUpNetworking();
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
		

		StackPane text_inputs = new StackPane();
		header.getChildren().add(text);
		
		
		input = new TextArea();
		input.setWrapText(true);
		input.setPrefHeight(17);
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
		
		input.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent ke) {
		        if (ke.getCode().equals(KeyCode.ENTER)) {
		        	writer.println(input.getText());
					writer.flush();
					text.setText("");
					text.requestFocus();
		            ke.consume();
		        }
		    }
		});

		
		StackPane inputs = new StackPane();
		inputs.getChildren().add(input);
		StackPane.setAlignment(input, Pos.CENTER_LEFT);
		//inputs.setMinWidth(primScreenBounds.getWidth()/3);
		inputs.setPadding(new Insets(20, 40, 40, 40));
		
		container.getChildren().add(header);
		container.getChildren().add(text);
		container.getChildren().add(inputs);
		
		controls.setContent(container);

        
        
        
        
        
		Scene scene1 = new Scene(controls);
        primaryStage.setX(0.0);
        primaryStage.setY(0.0);
		primaryStage.setScene(scene1);
		primaryStage.show();

	}

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("127.0.0.1", 4242);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			writer.println(input.getText());
			writer.flush();
			text.setText("");
			text.requestFocus();
		}
	}

	public static void main(String[] args) {
		try {
			new TestClient().run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
						text.setText(message + "\n");
						
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
