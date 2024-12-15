package party.heartbeat;

import main.Main;

/**
 * Implements the methods that members use to know if the host is down or
 * disconnected
 */
public class MemberHeartbeat extends Heartbeat {

    /**
     * Let main thread know it's been too long since the last message from the host
     * and stops the heartbveat thread
     */
    @Override
    protected void adjustHeartbeat() {
        Main main = Main.getInstance();

        main.notHeardFromHost();

        // After this, the thread will stop
        repeat = false;
    }

    @Override
    protected long MAX_DISTANCE() {
        return 2000;
    }

    @Override
    protected int SLEEP_SEC() {
        return 40;
    }
}
