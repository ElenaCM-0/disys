package music_player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import party.Action;
import utils.SongInstant;

/**
 * Class that will handle all updates made to the music player
 */
public class MusicPlayerTask {

    private MusicPlayer mp; // Music player object that plays the music
    private List<Update> updates = new ArrayList<>(); // List of updates made
    private final Lock lock = new ReentrantLock(); // Lock that controls no more than one thread updates the music
                                                   // player at the same time

    /**
     * Creates a MusicPlayerTak with an existing MusicPlayer
     * 
     * @param mp Already initialized music player
     */
    public MusicPlayerTask(MusicPlayer mp) {
        this.mp = mp;
    }

    /**
     * Creates an update reflecting the changes occured in the music player after
     * applying an action at a determined time
     * 
     * @param action        action to execute
     * @param executionTime time at which the action will be executed
     * @return Update object with the mentioned changes reflected or null if that
     *         action can not be executed for some reason
     */
    private Update createUpdate(Action action, long executionTime) {
        PlayerStatus prevStatus = getStatus(executionTime);
        PlayerStatus newStatus = action.apply(this.mp, prevStatus);
        if (newStatus == null) {
            return null;
        }
        return new Update(newStatus, executionTime);
    }

    /**
     * Gets the song name and the instant of the song that will be played at a
     * certain moment
     * 
     * @param time moment when the information is to be obtained (ms since Epoch)
     * @return SongInstant object with the mentioned information
     */
    public SongInstant getPosition(long time) {
        return getStatus(time).getInstant();

    }

    /**
     * @param time The moment when this status is to be obtained
     * @return The status that the player will have at a specified time
     */
    public PlayerStatus getStatus(long time) {
        /*
         * If there are no updates, we will get the status taking into account the
         * current status
         */
        if (updates.isEmpty()) {
            return new PlayerStatus(this.mp.getSongInstantFromNow(time), Status.PLAYING);
        }

        /*
         * Else, we will use the information of the last update as the last known status
         */
        Update lastUpdate = this.updates.getLast();
        SongInstant lastInstant = lastUpdate.geSongInstant();

        /* If the music was paused, the status won't change */
        if (lastUpdate.getStatus() == Status.PAUSED) {
            return new PlayerStatus(lastInstant, Status.PAUSED);
        }

        /*
         * Else, we will calculate what the song instant will be and set the status of
         * the music to PLAYING
         */
        return new PlayerStatus(this.mp.getSongInstantFrom(lastInstant, lastUpdate.getExecutionTime(), time),
                Status.PLAYING);
    }

    /**
     * Schedules a change to be done with the information provided by parameter
     * 
     * @param update Update object with contains the necessery information to make
     *               the change
     */
    public void addChange(Update update) {
        /* First, we get the information from the update if its not null */
        if (update == null) {
            return;
        }
        long executionTime = update.getExecutionTime();
        PlayerStatus newStatus = new PlayerStatus(update.geSongInstant(), update.getStatus());

        /*
         * We don't have to do anything if there is no actual update, but we add it to
         * the list so we know we have received or send an update
         */
        if (this.getStatus(executionTime).equals(newStatus)) {
            this.updates.add(update);
            return;
        }

        /* Otherwise, we schedule a thread to make the changes given by the update */
        this.updates.add(update);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long delayInMillis = executionTime - Instant.now().toEpochMilli();
        scheduler.schedule(() -> {
            this.mp.update(newStatus);
            scheduler.shutdown();
        }, delayInMillis, TimeUnit.MILLISECONDS);

    }

    /**
     * Creates an update reflecting the changes occured in the music player after
     * applying an action at a determined time, and schedules it to happen
     * 
     * @param action        action to execute
     * @param executionTime time at which the action will be executed
     * @return Update object with the mentioned changes reflected or null if that
     *         action can not be executed for some reason
     */
    public Update createAndAddUpdate(Action action, long executionTime) {
        /*
         * We get the lock to make sure no other thread is making an update at the same
         * time
         */
        lock.lock();
        Update newUpdate = this.createUpdate(action, executionTime);
        this.addChange(newUpdate);
        lock.unlock();
        return newUpdate;
    }

    /**
     * Schedules a thread to start a playing party at a determined time
     * 
     * @param executionTime time when the party will start
     */
    public void start(long executionTime) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long delayInMillis = executionTime - Instant.now().toEpochMilli();
        scheduler.schedule(() -> {
            Platform.startup(() -> {
                this.mp.start();
                this.mp.play();
                scheduler.shutdown();
            });
        }, delayInMillis, TimeUnit.MILLISECONDS);
    }

    /*
     * Stops the music player, freeing resources
     */
    public void stop() {
        this.mp.stop();
    }

}
