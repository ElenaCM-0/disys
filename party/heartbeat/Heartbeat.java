package party.heartbeat;

import java.time.Instant;

/**
 * Class that contains the common attributes and methods that a host uses to
 * know if there is need for the members to know that it's still up, and that
 * members use to know if the host is down or disconnected
 */
public abstract class Heartbeat implements Runnable {
    protected long latestUpdate;
    protected boolean repeat = true;

    /**
     * This method will update the heartbeat with the lastest contact
     * 
     * @param time time when the change is to be made according to the last message
     */
    public void lastUpdate(long time) {
        if (time > latestUpdate)
            latestUpdate = time;
    }

    /**
     * Checks periodically when the last message has been sent/received, and do the
     * necessery actions if too long has passed
     */
    @Override
    public void run() {
        try {
            while (repeat) {
                Thread.sleep(1000 * SLEEP_SEC());

                if ((Instant.now().toEpochMilli() - latestUpdate) < MAX_DISTANCE())
                    continue;

                /* It has been too long since the last update */

                adjustHeartbeat();
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    protected abstract void adjustHeartbeat();

    // TODO
    protected abstract long MAX_DISTANCE();

    protected abstract int SLEEP_SEC();
}
