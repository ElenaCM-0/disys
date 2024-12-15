package party;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import main.Main;
import music_player.Update;
import p2p.P2PConnection;
import utils.Connection;
import utils.MessageType;
import utils.MySocket;

/**
 * Class that represents a connection from a host to a regular member of a
 * party
 */
public class HostConnection extends Connection {
    private static Map<HostConnection, Thread> members = new HashMap<>(); // Keeps track of the connections to all
                                                                          // members of a party and the threads which
                                                                          // execute their run function

    /**
     * Assigns socket to a new connection and sets peer to null
     * 
     * @param socket already initialized socket
     * @throws UnknownHostException
     * @throws IOException
     */
    public HostConnection(MySocket socket) throws UnknownHostException, IOException {
        super(null, socket);
    }

    /**
     * Method that changes the given connection to a host connection and adds it to
     * the map
     * 
     * @param connection the connection to be added to the host
     * @throws IOException
     * @throws UnknownHostException
     */
    public static void addMember(P2PConnection connection) throws UnknownHostException, IOException {
        members.put(new HostConnection(connection.getSocket()), null);
    }

    /**
     * Method that changes the given connection to a host connection and adds it to
     * the map
     * 
     * @param connection the connection to be added to the host
     * @throws IOException
     * @throws UnknownHostException
     */
    public static void clearMembers() {
        members.clear();
    }

    /**
     * Receives action requests from a member and ask the rest of the members of the
     * party to execute them. This is done until it is interrumpted, the socket is
     * closed or an exception occurs
     */
    @Override
    public void run() {
        JSONObject message;
        Action action;

        try {
            while ((message = socket.receive(MessageType.ACTION_REQUEST.toString())) != null) {
                action = Action.match(message.getString("command"));

                if (action == null) {
                    /* Malformed message, continue waiting */
                    continue;
                }

                sendActionRequest(action);
            }

            return;
        } catch (InterruptedException e) {
            return;
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Caclculates the time when the next action can be executed. Then creates and
     * sends an update object with the new status of the music player to the rest of
     * the members of the party
     * 
     * @param act action to be executed
     */
    public static void sendActionRequest(Action act) {
        Main main = Main.getInstance();
        long time = main.getNearestChange();

        Update update = main.getMusicPlayerTask().createAndAddUpdate(act, time);
        if (update == null) {
            /* The update could not be created for some reason */
            return;
        }

        sendUpdateToMembers(update);
    }

    /**
     * Start running all connections with the members of the party, creating new
     * threads if necessery
     */
    public static void startMembers() {
        members.replaceAll((conn, thr) -> {
            if (thr == null)
                thr = new Thread(conn);

            if (!thr.isAlive() && !conn.isClosed())
                thr.start();

            return thr;
        });
    }

    /**
     * Interrupt all connections with the members of a party and joins the threads
     */
    public static void joinMembers() {
        members.forEach((c, t) -> {
            try {
                if (t.isAlive())
                    t.interrupt();
                t.join();
            } catch (InterruptedException e) {
                return;
            }
        });
    }

    /**
     * Sends a message with the information of an update to all members of a party.
     * It also updates the time when the host sent the last message
     * 
     * @param update Update object with the information to send
     */
    public static void sendUpdateToMembers(Update update) {
        JSONObject message = update.createUpdateJSON();

        message.put("type", MessageType.EXECUTE_ACTION.toString());

        Main.getInstance().getHeartbeat().lastUpdate(Instant.now().toEpochMilli());

        try {
            sendToMembers(message);
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Method that sends the given message to all the member connections
     * 
     * @param message message to send
     */
    private static void sendToMembers(JSONObject message) throws IOException {
        for (HostConnection member : members.keySet()) {
            member.send(message);
        }
    }

    /**
     * Sends a message with the time when a party will start to all members of such
     * party
     * 
     * @param start_time time to start the party (miliseconds since Epoch)
     * @throws IOException
     */
    public static void sendStartParty(long start_time) throws IOException {
        JSONObject message = new JSONObject();

        message.put("type", MessageType.START_PARTY.toString());
        message.put("time", start_time);

        sendToMembers(message);
    }

}
