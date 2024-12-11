package party.heartbeat;

import main.Main;
import music_player.PlayerStatus;
import music_player.Update;
import party.HostConnection;

public class HostHeartbeat extends Heartbeat {

    @Override
    protected void adjustHeartbeat() {
        Main main = Main.getInstance();
        Long nearestChange = main.getNearestChange();

        PlayerStatus status = main.getMusicPlayerTask().getStatus(nearestChange);

        /* Create message to send to the other nodes */
        System.out.println("Sending heartbeat to users, for time " + nearestChange);
        HostConnection.sendUpdateToMembers(new Update(status, nearestChange));
    }

    @Override
    protected long MAX_DISTANCE() {
        return 1000;
    }

    @Override
    protected int SLEEP_SEC() {
        return 15;
    }

}
