package party;

import java.io.IOException;
import java.net.UnknownHostException;
import org.json.JSONObject;

import main.Main;
import music_player.Update;
import utils.MessageType;
import utils.MySocket;

public class HostConnection extends PartyConnection {

    public HostConnection(String peer, MySocket socket) throws UnknownHostException, IOException {
        super(peer, socket);
    }

    @Override
    public void run() {
        JSONObject message;
        Action action;

        try {
            while ((message = socket.receive("action_request")) != null) {
                action = Action.match(message.getString("command"));

                if (action == null) {
                    /* Malformed message, continue waiting */
                    continue;
                }

                sendActionRequest(action);
            }

            return;
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }
    }

    @Override
    public void sendActionRequest(Action act) {
        Main main = Main.getInstance();
        long time = main.getNearestChange();

        Update update = main.getMusicPlayerTask().createUpdate(act, time);

        main.getHeartbeat().lastUpdate(time);

        sendUpdateToMembers(update, main);

        main.getMusicPlayerTask().addChange(update);
    }

    public static void sendUpdateToMembers(Update update, Main main) {
        JSONObject message = update.createUpdateJSON();

        message.put("type", MessageType.EXECUTE_ACTION.toString());

        try {
            main.sendToAllConnections(message);
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }
    }

}
