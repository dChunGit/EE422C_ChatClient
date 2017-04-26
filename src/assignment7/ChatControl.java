package assignment7;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ChatControl extends Application{
	
	public static void main(String[] args) {
    	launch(args);
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
		
		
		TextArea text = new TextArea();
		text.setText("I barely started learning javaFX today, and I'm really confused about the text node. The text I made won't align in the center. I tried using a Pane, GridPane, and now a VBox. Does that have anything to do with it?  -fx-text-al"
				+ "ignment: center; But that didn't work either. Im really new to this. Thank you anyone for the help! Here is my code:"
				+ "I barely started learning javaFX today, and I'm really confused about the text node. The text I made won't align in the center. I tried using a Pane, GridPane, and now a VBox. Does that have anything to do with it?  -fx-text-al"
				+ "ignment: center; But that didn't work either. Im really new to this. Thank you anyone for the help! Here is my code:"
				+ "I barely started learning javaFX today, and I'm really confused about the text node. The text I made won't align in the center. I tried using a Pane, GridPane, and now a VBox. Does that have anything to do with it?  -fx-text-al"
				+ "ignment: center; But that didn't work either. Im really new to this. Thank you anyone for the help! Here is my code:");
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
		
		
		TextArea input = new TextArea();
		input.setWrapText(true);
		input.setPrefHeight(17);
		input.setMaxHeight(70);
		
		input.setPrefWidth(primScreenBounds.getWidth()/6);
		input.setText("Trial");
		
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
		        	String text = input.getText();
		            System.out.println(text);
		            input.clear();

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
	
	

}
