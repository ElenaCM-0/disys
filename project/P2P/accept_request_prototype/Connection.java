package P2P.accept_request_prototype;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

import org.json.JSONObject;

import utils.MySocket;


// Using Elenas code as template for this, some modifications has been done

public class Connection extends MySocket implements Runnable {
    private String peer;
    private boolean amFriend;

    public Connection(String peer, boolean friend, String ip, int port) throws UnknownHostException, IOException{
        super(ip, port);
        this.peer = peer;
        this.amFriend = friend;
    }

    public Connection(String peer, boolean friend, Socket sock) throws UnknownHostException, IOException{
        super(sock);
        this.peer = peer;
        this.amFriend = friend;
    }


    @Override
    public void run() {
        JSONObject message;

        try {
            while ((message = receive()) != null) {
                switch (message.getString("type")) {
                    case "request_message":
                        if (!amFriend) {
                            /* The node who sent the message is not a friend, discard */
                            continue;
                        }


                        // respond back with "accepted" status
                        JSONObject response = new JSONObject();
                        response.put("type", "response_message");
                        response.put("status", "accepted");
                        send(response);
                        System.out.println(
                            "Sent response: accepted"
                            );
                        break;

                    case "response_message":
                        // prints the response message received from the host
                        String status = message.getString("status");
                        System.out.println(
                            "Handling response_message: " + message.toString()
                            );
                        System.out.println(
                            "Received response: " + status
                            );
                        break;

                    
                    default:
                        System.out.println("Unknown message type: " + message.getString("type"));
                            continue;
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();

            return; 
        }

        
    }


    // method for sending messages
    public void send(JSONObject message) {
    try {
        // sends the message to the peer through the socket
        getSocket().getOutputStream().write(message.toString().getBytes());
    } 

    catch (IOException e) {
        System.out.println(
            "Error sending message: " + e.getMessage()
            );
        e.printStackTrace();
    }
}

    
}
