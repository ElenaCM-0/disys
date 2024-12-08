package p2p;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import main.Main;
import utils.Connection;
import utils.MessageType;
import utils.MySocket;
import utils.SharedInfo;

public class P2PConnection extends Connection {
    private static long max_response_time;
    private long sent_time;

    public P2PConnection(String peer, MySocket socket) throws UnknownHostException, IOException {
        super(peer, socket);
    }

    public P2PConnection(String peer, String ip, int port) throws UnknownHostException, IOException {
        super(peer, ip, port);
    }

    @Override
    public void run() {
        System.out.println("Waiting for " + peer + " to send a message");
        try {
            while (!Thread.interrupted()) {
                // Receive message
                JSONObject message = socket.receive();

                MessageType type = MessageType.match(message.getString("type"));

                switch (type) {
                    case PARTY_REQUEST:
                        if (processPartyRequest(message))
                            return;
                        break;
                    case PARTY_RESPONSE:
                        if (processPartyResponse())
                            return;

                        break;
                    default:
                        break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if the thread should finish execution after calling the method,
     *         false otherwise
     */
    private boolean processPartyRequest(JSONObject message) {
        Main main = Main.getInstance();

        SharedInfo request = main.getRequest();

        request.acquireLock();

        /* Wait to talk to the user */
        main.requestMain();

        if (main.getStatus() != Main.MAIN_STATUS.P2P) {
            main.releaseMain();
            return false;
        }

        request.setWaitingConnection(this);

        main.askUser(peer + " is hosting a playin party, do you want to join?");

        request.setAnswer(null);

        Boolean answer;

        while ((answer = request.getAnswer()) == null);

        request.releaseLock();

        if (answer == false)
            return false;

        int num_songs = message.getInt("num_songs");

        List<String> song_list = new ArrayList<>();

        for (int i = 0; i < num_songs; i++) {
            song_list.add(message.getString("song_" + i));
        }

        main.addPartySongs(song_list);

        return true;
    }

    /**
     * @return true if the thread should finish execution after calling the method,
     *         false otherwise
     */
    private boolean processPartyResponse() {
        sent_time = Instant.now().toEpochMilli() - sent_time;
        
        Main main = Main.getInstance();

        SharedInfo answer = main.getResponse();

        answer.acquireLock();

        main.requestMain();

        if (main.getStatus() != Main.MAIN_STATUS.HOST) {
            main.releaseMain();
            return false;
        }

        answer.setWaitingConnection(this);

        answer.setAnswer(null);

        main.askUser(peer + " wants to join your party, accept?");

        if (sent_time > max_response_time) {
            max_response_time = sent_time;
        }

        return true;
    }

    /**
     * This method restarts the time counter for the P2P connection class, the one that will keep track of how long the nodes take to respond
     */
    public static void restartTime() {
        max_response_time = 0;
    }

    /**
     * Method that sends the party request, it will save the time when the message was sent
      * @throws IOException 
      */
     public void sendPartyRequest(JSONObject request) throws IOException {
        socket.send(request);
        sent_time = Instant.now().toEpochMilli();
    }
}
