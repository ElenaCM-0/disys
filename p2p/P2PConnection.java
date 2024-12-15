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

/**
 * Class that represents a P2P Connection in the network
 */
public class P2PConnection extends Connection {
    private static long max_response_time; // Keeps track of the maximum time time a peer took to respond (milisecons
                                           // since Epoch)
    private long sent_time; // Time when the last message was sent (milisecons since Epoch)
    private Long party_time; // Time when a party will start

    /**
     * Assigns socket and peer to a new connection
     * 
     * @param peer   name or id of the peer to which the socket is connected
     * @param socket already initialized socket
     * @throws UnknownHostException
     * @throws IOException
     */
    public P2PConnection(String peer, MySocket socket) throws UnknownHostException, IOException {
        super(peer, socket);
    }

    /**
     * Creates a socket with indicated ip and port and assigns it to a
     * connection
     * 
     * @param peer name or id of the peer corresponding to the ip and port
     * @param ip   ip to connect to
     * @param port port to connect to
     * @throws UnknownHostException
     * @throws IOException
     */
    public P2PConnection(String peer, String ip, int port) throws UnknownHostException, IOException {
        super(peer, ip, port);
    }

    /**
     * Receives and process messages from other peers until a party starts, it is
     * interrupted or an exception occurs
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                JSONObject message = socket.receive();

                if (message == null) {
                    /* Socket is closed */
                    return;
                }

                /* Get message type and process it */
                MessageType type = MessageType.match(message.getString("type"));

                switch (type) {
                    case PARTY_REQUEST:
                        processPartyRequest(message);

                        break;
                    case PARTY_RESPONSE:
                        if (processPartyResponse())
                            return;

                        break;
                    case START_PARTY:
                        if (processStartParty(message))
                            return;
                        break;

                    default:
                        break;
                }

            }
        } catch (InterruptedException e) {
            return;
        } catch (IOException e) {
            return;
        }
    }

    /**
     * It processes a party request message which means:
     * -Ask the user if they want to join the party
     * -If so, it sends the answer to the host and lets the main thread know the
     * party songs
     * 
     * @param message received message
     * @throws IOException
     */
    private void processPartyRequest(JSONObject message) throws IOException {
        Main main = Main.getInstance();

        SharedInfo request = main.getRequest();

        /* Get the lock to modify shared info */
        request.acquireLock();

        /* Wait to talk to the user */
        main.requestMain();

        /* If the status changes while it was waiting, ignore message */
        if (main.getStatus() != Main.MAIN_STATUS.P2P) {
            main.unlockMain();
            request.releaseLock();
            return;
        }

        /* Ask user and wait for answer */
        request.setWaitingConnection(this);
        request.setAnswer(null);
        main.askUser(peer + " is hosting a playing party, do you want to join?");

        Boolean answer;

        while ((answer = request.getAnswer()) == null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                main.unlockMain();
                request.releaseLock();
                return;
            }

        }

        main.releaseMain();
        request.releaseLock();

        /* Return if the user doesn't want to join */
        if (answer == false)
            return;

        /* Send answer to host if the user has said yes */
        JSONObject newMessage = new JSONObject();
        newMessage.put("type", MessageType.PARTY_RESPONSE.toString());
        send(newMessage);

        /* Create and add the list of songs for the party to main thread */
        int num_songs = message.getInt("num_songs");

        List<String> song_list = new ArrayList<>();

        for (int i = 0; i < num_songs; i++) {
            song_list.add(message.getString("song_" + i));
        }

        main.addPartySongs(song_list);

        return;
    }

    /**
     * It processes a party response, asking the user if they accept a peer in the
     * party
     * 
     * @return true if the thread should finish execution after calling the method,
     *         false otherwise
     */
    private boolean processPartyResponse() {
        /* Get the time since the request was sent */
        sent_time = Instant.now().toEpochMilli() - sent_time;

        /* Get locks to modify shared info and ask user */
        Main main = Main.getInstance();

        SharedInfo answer = main.getResponse();

        answer.acquireLock();

        main.requestMain();

        /* If the status changes while it was waiting, ignore message */
        if (main.getStatus() != Main.MAIN_STATUS.HOST) {
            main.unlockMain();
            answer.releaseLock();
            return false;
        }

        /* Ask user */
        answer.setWaitingConnection(this);
        answer.setAnswer(null);
        main.askUser(peer + " wants to join your party, accept?");
        main.releaseMain();
        answer.releaseLock();

        /* Update maximum response time if needed */
        if (sent_time > max_response_time) {
            max_response_time = sent_time;
        }

        return true;
    }

    /**
     * It processes a start party message, leting main thread when the party will
     * start
     * 
     * @param message message received
     * @return true if the execution has to finnish, false otherwise
     */
    private boolean processStartParty(JSONObject message) {

        /* Wait to contact with main thread */
        Main main = Main.getInstance();

        main.requestMain();

        /* If the status changes while it was waiting, ignore message */
        if (main.getStatus() != Main.MAIN_STATUS.JOIN) {
            main.unlockMain();
            return false;
        }

        /* Let main thread know the time when the party will start */
        party_time = message.getLong("time");
        main.askUser(party_time.toString());
        main.releaseMain();
        return true;
    }

    /**
     * @return maximum time a peer has taken to respond
     */
    public static long getMaxTime() {
        return max_response_time;
    }

    /**
     * This method restarts the time counter for the P2P connection class, the one
     * that will keep track of how long the nodes take to respond
     */
    public static void restartTime() {
        max_response_time = 0;
    }

    /**
     * Method that sends the party request, it will save the time when the message
     * was sent
     * 
     * @throws IOException
     */
    public void sendPartyRequest(JSONObject request) throws IOException {
        socket.send(request);
        sent_time = Instant.now().toEpochMilli();
    }
}
