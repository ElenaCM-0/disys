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

    /**
     * In this case, the member nodes allow a long stretch of time between updates, as
     * we considered it more harmful to assume the host is not working when it is then 
     * assuming the host is down when it is not.
     */
    @Override
    protected long MAX_DISTANCE() {
        return 2000;
    }

    /**
     * For this same reason, the member nodes do not check on the host heartbeat as often
     * we ensure that they will eventually realise the host is not working, not necessarily 
     * that they will do so instantly.
     */
    @Override
    protected int SLEEP_SEC() {
        return 40;
    }
}
