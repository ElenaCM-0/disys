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

    /**
     * The child classes will overwrite this method with the actions that are to be taken if too long has passed
     */
    protected abstract void adjustHeartbeat();

    /**
     * This function will be overwritten in the child classes to return
     * how much time can pass between updates. That is, if the difference 
     * between the current time and the last update is greater than the 
     * amount returned by this function, then it is considered that
     * "too long has passed"
     */
    protected abstract long MAX_DISTANCE();

    /**
     * This function will be overwritten in the child classes to return the
     * amount of time the thread is to sleep before checking if the heartbeats
     * are working as expected 
     */
    protected abstract int SLEEP_SEC();
}
