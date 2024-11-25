package P2P;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

import org.json.JSONObject;

import utils.MySocket;

public class connection extends MySocket implements Runnable {
    private String peer;
    private boolean amFriend;

    public connection(String peer, boolean friend, String ip, int port) throws UnknownHostException, IOException{
        super(ip, port);
        this.peer = peer;
        this.amFriend = friend;
    }

    public connection(String peer, boolean friend, Socket sock) throws UnknownHostException, IOException{
        super(sock);
        this.peer = peer;
        this.amFriend = friend;
    }

    /**
     * This method will check the user's friend list and verify if the node this connection is with is added as a friend
     * @return true if the connected node is a friend, false otherwisee
    
    private boolean amFriend() {
        try {
            File myObj = new File("filename.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                System.out.println(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
     */

    /**
     * When this method is called, a new thread will listen in on the messages sent through this connection,
     * dealing with them as they are received
     */
    @Override
    public void run() {
        JSONObject message;

        try {
            while ((message = receive()) != null) {
                switch (message.getString("type")) {
                    case "request_message":{
                        if (!amFriend) {
                            /* The node who sent the message is not a friend, discard */
                            continue;
                        }

                        /* Show the message to the user */
                    }
                    default: {
                        /* Malformed message, discard */
                        continue;
                    }
                }
                

            }
        } catch (IOException e) {
            e.printStackTrace();

            return; 
        }

        
    }
    
}
