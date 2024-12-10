package party;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import main.Main;
import music_player.Update;
import p2p.P2PConnection;
import utils.MessageType;
import utils.MySocket;

public class HostConnection extends PartyConnection {
    private static Map<HostConnection, Thread> members = new HashMap<>();
    public HostConnection(MySocket socket) throws UnknownHostException, IOException {
        super(null, socket);
    }

    /** 
     * Method that changes the given connection to a host connection and adds it to the map
     * @param connection the connection to be added to the host
     * @throws IOException 
     * @throws UnknownHostException 
    */
    public static void addMember(P2PConnection connection) throws UnknownHostException, IOException {
        members.put(new HostConnection(connection.getSocket()), null);
    }

    /** 
     * Method that changes the given connection to a host connection and adds it to the map
     * @param connection the connection to be added to the host
     * @throws IOException 
     * @throws UnknownHostException 
    */
    public static void clearMembers() {
        members.clear();
    }

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
            e.printStackTrace();

            return;
        }
    }

    public static void sendActionRequest(Action act) {
        Main main = Main.getInstance();
        long time = main.getNearestChange();

        Update update = main.getMusicPlayerTask().createAndAddUpdate(act, time);

        main.getHeartbeat().lastUpdate(time);

        sendUpdateToMembers(update, main);
    }

    public static void startMembers() {
        members.replaceAll((conn, thr) -> {
            if (thr == null) thr = new Thread(conn);

            if (!thr.isAlive() && !conn.isClosed()) thr.start();

            return thr;
        });
    }

    public static void joinMembers() {
        members.forEach((c, t) -> {
            try {
                if (t.isAlive()) t.interrupt();
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public static void sendUpdateToMembers(Update update, Main main) {
        JSONObject message = update.createUpdateJSON();

        message.put("type", MessageType.EXECUTE_ACTION.toString());

        try {
            sendToMembers(message);
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }
    }

    /**
     * Method that sends the given message to all the member connections
     * @param message
     */
    private static void sendToMembers(JSONObject message) throws IOException {
        for (HostConnection member: members.keySet()) {
            member.send(message);
        }
    }

    /**
     * Method that will send the start_party message
      * @throws IOException 
      */
     public static void sendStartParty(long start_time) throws IOException {
        JSONObject message = new JSONObject();

        message.put("type", MessageType.START_PARTY.toString());
        message.put("time", start_time);

        sendToMembers(message);
    }

}
