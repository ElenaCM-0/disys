package p2p;

import java.io.IOException;
import java.net.UnknownHostException;

import org.json.JSONObject;
import main.Main;
import utils.Connection;
import utils.MessageType;
import utils.MySocket;

public class P2PConnection extends Connection {

    public P2PConnection(String peer, MySocket socket) throws UnknownHostException, IOException {
        super(peer, socket);
    }

    public P2PConnection(String peer, String ip, int port) throws UnknownHostException, IOException {
        super(peer, ip, port);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                // receive message and determine its type
                JSONObject message = socket.receive(MessageType.PARTY_REQUEST.name());
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // handle action request from peer
    private void processActionRequest(JSONObject message) {
        String actionStr = message.getString("command");
        System.out.println("Processing action request: " + actionStr);
        // example: process the action (e.g., add change to music player)
        // here you could add logic to update the music player based on the action
    }

    // handle execute action from peer
    private void processExecuteAction(JSONObject message) {
        String songName = message.getString("song_name");
        long songTime = message.getLong("song_time");
        int totalUpdates = message.getInt("total_updates");

        // example: update the music player or log the action
        System.out.println("Executing action with song: " + songName + ", Time: " + songTime);
        // here you would apply the action to the music player state
    }

    // method to send a message to the peer
    public void sendMessage(JSONObject message) throws IOException {
        // set the message type for the outgoing message
        message.put("type", MessageType.EXECUTE_ACTION.toString());
        socket.send(message);
    }

    // close connection
    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
