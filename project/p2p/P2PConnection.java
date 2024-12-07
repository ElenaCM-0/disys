package p2p;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import main.Main;
import utils.Connection;
import utils.MessageType;
import utils.MySocket;
import utils.SharedInfo;

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
                // Receive message
                JSONObject message = socket.receive();

                MessageType type = MessageType.match(message.getString("type"));

                switch (type) {
                    case MessageType.PARTY_REQUEST:
                        if (processPartyRequest(message)) return;
                        break;
                    case MessageType.PARTY_RESPONSE:
                        if (processPartyResponse()) return;

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
     * @return true if the thread should finish execution after calling the method, false otherwise
     */
    private boolean processPartyRequest(JSONObject message) {
        Main main = Main.getInstance();

        if (main.getHost() != true) return false;

        SharedInfo request = main.getRequest();

        request.acquireLock();

        if (main.getInput()) {
            while (main.getHost() == null);

            if (main.getHost()) return false;
        }
        
        System.out.println(peer + " is hosting a playin party, do you want to join?");

        request.setWaitingConnection(this);

        request.setAnswer(null);

        Boolean answer;

        while ((answer = request.getAnswer()) == null);

        request.setWaitingConnection(null);

        request.releaseLock();

        if (answer == false) return false;

        int num_songs = message.getInt("num_songs");

        List<String> song_list = new ArrayList<>();

        for (int i = 0; i < num_songs; i++) {
            song_list.add(message.getString("song_" + i));
        }

        main.addPartySongs(song_list);

        return true;
    }

    /**
     * @return true if the thread should finish execution after calling the method, false otherwise
     */
    private boolean processPartyResponse() {
        Main main = Main.getInstance();

        if (main.getHost() != true) return false;

        SharedInfo answer = main.getResponse();

        answer.acquireLock();

        answer.setWaitingConnection(this);

        answer.setAnswer(null);

        System.out.println(peer + " wants to join your party, accept?");

        return true;
    }
}
