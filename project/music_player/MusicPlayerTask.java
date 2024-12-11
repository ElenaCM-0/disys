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

public class MusicPlayerTask {

    private MusicPlayer mp;
    private List<Update> updates = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

    public MusicPlayerTask(MusicPlayer mp) {
        this.mp = mp;
    }

    /**
     * Creates an update reflecting the changes occured in the music playerafter
     * applying an action ata a determined time
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
     * @return The status that the player will be in a specified time
     */
    public PlayerStatus getStatus(long time) {

        if (updates.isEmpty()) {
            /** Do we delete past updates? */
            /** TODO */
            return new PlayerStatus(this.mp.getSongInstantFromNow(time), Status.PLAYING);
        }

        Update lastUpdate = this.updates.getLast();
        SongInstant lastInstant = lastUpdate.geSongInstant();
        if (lastUpdate.getStatus() == Status.PAUSED) {
            return new PlayerStatus(lastInstant, Status.PAUSED);
        }

        return new PlayerStatus(this.mp.getSongInstantFrom(lastInstant, lastUpdate.getExecutionTime(), time),
                Status.PLAYING);
    }

    /**
     * Schedules a change to be done with the information provided by parameter
     * 
     * @param update Update object with contains the necessery information for the
     *               change
     */
    public void addChange(Update update) {
        long executionTime = update.getExecutionTime();
        PlayerStatus newStatus = new PlayerStatus(update.geSongInstant(), update.getStatus());
        // We don't have to do anything if there is no actual update
        if (this.getStatus(executionTime).equals(newStatus)) {
            this.updates.add(update);
            return;
        }
        this.updates.add(update);
        // Schedule update
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long delayInMillis = executionTime - Instant.now().toEpochMilli();
        // Programa la tarea
        scheduler.schedule(() -> {
            this.mp.update(newStatus);
            scheduler.shutdown();
        }, delayInMillis, TimeUnit.MILLISECONDS);

    }

    public Update createAndAddUpdate(Action action, long executionTime) {
        lock.lock();
        Update newUpdate = this.createUpdate(action, executionTime);
        this.addChange(newUpdate);
        lock.unlock();
        return newUpdate;
    }

    public void start(long executionTime) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long delayInMillis = executionTime - Instant.now().toEpochMilli();
        // Programa la tarea
        scheduler.schedule(() -> {
            Platform.startup(() -> {
                this.mp.start();
                this.mp.play();
                scheduler.shutdown();
            });
        }, delayInMillis, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        this.mp.stop();
    }

    // Example about how to implement doing an action in a certain UTC time. I
    // copied it just to have the idea abou how the process is done. This is not
    // final code at all
    public static void main(String[] args) throws InterruptedException {
        List<String> songs = new ArrayList<>();
        songs.add("song1.mp3");
        songs.add("song2.mp3");
        MusicPlayer mp = new MusicPlayer(songs);
        MusicPlayerTask mpt = new MusicPlayerTask(mp);
        mpt.start(1000);
        Thread.sleep(4000);
        long time = Instant.now().toEpochMilli() + 3000;
        time = time + 5000;
        mpt.createAndAddUpdate(Action.PAUSE, time);
        time = time + 3000;
        mpt.createAndAddUpdate(Action.PLAY, time);
        Thread.sleep(300000);
        mp.stop();
    }

}
