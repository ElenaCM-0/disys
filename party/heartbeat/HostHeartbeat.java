package party.heartbeat;

import main.Main;
import music_player.PlayerStatus;
import music_player.Update;
import party.HostConnection;

/**
 * Class that implements the methods that a host uses to know if there is need
 * for the members to know that it's still up
 */
public class HostHeartbeat extends Heartbeat {

    /**
     * Sends a message to the members letting them know about the status of the
     * music player after some time
     */
    @Override
    protected void adjustHeartbeat() {
        Main main = Main.getInstance();
        Long nearestChange = main.getNearestChange();

        PlayerStatus status = main.getMusicPlayerTask().getStatus(nearestChange);

        /* Create message to send to the other nodes */
        HostConnection.sendUpdateToMembers(new Update(status, nearestChange));
    }

    /**
     * In the host heartbeat, we allow very little tolerance to ensure no nodes
     * will assume the host is down incorrectly
     */
    @Override
    protected long MAX_DISTANCE() {
        return 100;
    }

    /**
     * The host thread also checks the update frequency often, for the same reason
     */
    @Override
    protected int SLEEP_SEC() {
        return 10;
    }

}
