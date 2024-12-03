package p2p;

import java.io.IOException;
import org.json.JSONObject;
import main.Main;

import utils.MessageType;
import utils.MySocket;


public class P2PConnection extends Thread {
    private MySocket socket;
    private String peer;

    public P2PConnection(String peer, MySocket socket) {
        this.peer = peer;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                // receive message and determine its type
                JSONObject message = socket.receive();
                if (message != null) {
                    MessageType messageType = MessageType.valueOf(message.getString("type").toUpperCase());

                    // handle the message based on its type
                    switch (messageType) {
                        case ACTION_REQUEST:
                            processActionRequest(message);
                            break;
                        case EXECUTE_ACTION:
                            processExecuteAction(message);
                            break;
                        default:
                            System.out.println("Unsupported message type: " + messageType);
                            break;
                    }
                }
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
