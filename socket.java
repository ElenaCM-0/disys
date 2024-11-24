import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import netscape.javascript.JSObject;

public class socket {
    private Socket tunnel;
    private OutputStreamWriter out;
    private InputStreamReader in;

    /**
     * It will create the socket connection
     * @param ip The ip address the socket will be connected to
     * @param port The port the socket will connect to
     */
    public socket(String ip, int port) throws UnknownHostException, IOException{
        tunnel = new Socket(ip, port);
        try {
            out = new OutputStreamWriter(tunnel.getOutputStream(), StandardCharsets.UTF_8);
            in = new InputStreamReader(tunnel.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }
    }

    /**
     * Sends a message through the socket
     * @param message JSON message that is to be sent
     * @throws IOException
     */
    public void send(JSObject message) throws IOException {
        out.write(message.toString());
    }

    /**
     * Receives a message through the socket
     * A call to this method will not return
     * 
     * @return The JSON object that was received
     * @throws IOException
     */
    public JSObject receive() {
        try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                
                String jsonMessage;
                while ((jsonMessage = reader.readLine()) != null) { // Read line-by-line
                    System.out.println("Received JSON: " + jsonMessage);

                    // Parse the JSON using JSONObject
                    JSONObject jsonObject = new JSONObject(jsonMessage);
                    String type = jsonObject.getString("type");
                    String message = jsonObject.getString("message");

                    System.out.println("Type: " + type);
                    System.out.println("Message: " + message);
                }
            }
    }
}
