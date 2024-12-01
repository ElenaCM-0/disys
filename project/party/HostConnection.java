package party;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;

import org.json.JSONObject;

import main.Main;
import utils.MySocket;

public class HostConnection extends PartyConnection{

    public HostConnection(String peer, MySocket socket) throws UnknownHostException, IOException {
        super(peer, socket);
    }

    @Override
    public void run() {
        JSONObject message;
        Action action;

        try {
            while((message = socket.receive("action_request")) != null){
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
        int seconds_ahead = Main.getInstance().getSeconds();
        long new_time = Instant.now().getEpochSecond() + seconds_ahead;
        
        
    }
    
}
