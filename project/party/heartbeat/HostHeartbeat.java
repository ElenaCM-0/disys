package party.heartbeat;

import java.io.IOException;

import org.json.JSONObject;

import main.Main;
import music_player.PlayerStatus;
import music_player.Update;
import party.HostConnection;

public class HostHeartbeat extends Heartbeat{

    @Override
    protected void adjustHeartbeat() {
        Main main = Main.getInstance();
        Long nearestChange = main.getNearestChange();

        PlayerStatus status = main.getMusicPlayerThread().getStatus(nearestChange);

        /* Create message to send to the other nodes */

        HostConnection.sendUpdateToMembers(new Update(status, nearestChange), main);
    }

    @Override
    protected long MAX_DISTANCE() {return 5;}
    
    @Override
    protected int SLEEP_SEC() {return 25;}

}
