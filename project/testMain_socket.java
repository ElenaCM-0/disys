import java.util.concurrent.TimeUnit;

import org.json.*;

import utils.MySocket;

public class testMain_socket {

	public static void main(String[] args) throws Exception {
		Boolean hooray = false;

		while(!hooray) {
			try {
				MySocket waiting = new MySocket(null, Integer.valueOf(args[0]));

				JSONObject message = new JSONObject();
				
				message.put("Hello", "World client");
		
				waiting.send(message);

				System.out.println("message sent");
				
				message = waiting.receive();

				System.out.print(message);

				hooray = true;
			} catch (java.net.ConnectException e) {
				TimeUnit.SECONDS.sleep(1);
			}
		}
	}

}
