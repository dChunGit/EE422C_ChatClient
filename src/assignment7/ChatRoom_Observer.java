package assignment7;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;

public class ChatRoom_Observer extends PrintWriter implements Observer {
	public ChatRoom_Observer(OutputStream out) {
		super(out);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		System.out.println(arg);
		this.println(arg); //writer.println(arg);
		this.flush(); //writer.flush();
	}

}