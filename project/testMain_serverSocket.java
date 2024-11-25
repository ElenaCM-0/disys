import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import org.json.*;

import utils.MySocket;

public class testMain_serverSocket {

	public static void main(String[] args) throws Exception {
		Boolean hooray = false;

		while(!hooray) {
			try {
				ServerSocket temp = new ServerSocket(Integer.valueOf(args[0]));
				MySocket waiting = new MySocket(temp.accept());

				temp.close();

				JSONObject message = new JSONObject();
				
				message.put("Hello", "World");
		
				waiting.send(message);

				System.out.println("message sent");
				
				message = waiting.receive();

				hooray = true;
			} catch (java.net.ConnectException e) {
				TimeUnit.SECONDS.sleep(1);
			}
		}
	}
}
