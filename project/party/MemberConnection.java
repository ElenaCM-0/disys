package party;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import utils.MessageType;
import org.json.JSONObject;

import main.Main;
import music_player.Update;

public class MemberConnection extends PartyConnection {

    public MemberConnection(String peer, String ip, int port) throws UnknownHostException, IOException {
        super(peer, ip, port);
    }

    public MemberConnection(String peer, Socket sock) throws UnknownHostException, IOException {
        super(peer, sock);
    }

    @Override
    public void run() {
        JSONObject message;

        try {
            while ((message = socket.receive(MessageType.EXECUTE_ACTION.toString())) != null) {
                Update update = Update.parsefromJSON(message);
                Main.getInstance().getMusicPlayerThread().addChange(update);
            }

            return;
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }

    }

    @Override
    public void sendActionRequest(Action act) throws IOException {
        JSONObject message = new JSONObject();
        message.put("type", MessageType.ACTION_REQUEST.toString());
        message.put("command", act.toString());
        this.socket.send(message);
    }
}