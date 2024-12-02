package utils;

import java.io.IOException;
import java.net.*;

import org.json.JSONObject;

public class Connection implements Runnable {
    private String peer;
    protected MySocket socket;

    public Connection(String peer, String ip, int port) throws UnknownHostException, IOException{
        socket = new MySocket(ip, port);
        this.peer = peer;
    }

    public Connection(String peer, Socket sock) throws UnknownHostException, IOException{
        socket = new MySocket(sock);
        this.peer = peer;
    }

    protected Connection(String peer, MySocket sock) throws UnknownHostException, IOException{
        socket = sock;
        this.peer = peer;
    }

    /**
     * Method that sends the given message through the connection
    * @throws IOException 
    */
    public void send(JSONObject message) throws IOException {
        socket.send(message);
    }

    /**
     * When this method is called, a new thread will listen in on the messages sent through this connection,
     * dealing with them as they are received
     */
    @Override
    public void run() {
        JSONObject message;

        try {
            while ((message = socket.receive()) != null) {
                switch (message.getString("type")) {
                    case "request_message":{

                        /* Show the message to the user */
                    }
                    default: {
                        /* Malformed message, discard */
                        continue;
                    }
                }
                

            }

            return;
        } catch (IOException e) {
            e.printStackTrace();

            return; 
        }

        
    }
    
}
