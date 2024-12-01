package party.heartbeat;

import java.time.Instant;

public abstract class Heartbeat implements Runnable {
    protected long latestUpdate;

    /**
     * This method will update the heartbeat with the lastest contact
     * 
     * @param time time when the change is to be made according to the last message
     */
    public void lastUpdate(long time) {
        if (time > latestUpdate)
            latestUpdate = time;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(1000 * SLEEP_SEC());

                if (Math.abs(Instant.now().getEpochSecond() - latestUpdate) <= MAX_DISTANCE())
                    continue;

                /* It has been too long since the last update */

                adjustHeartbeat();

            }
        } catch (InterruptedException e) {
            return;
        }
    }

    protected abstract void adjustHeartbeat();

    protected abstract long MAX_DISTANCE();

    protected abstract int SLEEP_SEC();
}
