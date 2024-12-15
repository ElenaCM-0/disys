package music_player;

import java.time.Instant;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import utils.SongInstant;

/**
 * Class that represents a music player, implementing all necessery methods for
 * the music to be handled
 */
public class MusicPlayer {
    private static final String BASE_URL = "../songs/";// Base URL to find the songs
    private List<String> songs;// Songs that will be played
    private MediaPlayer player;// Media player object which actually plays music
    private int currentIndex = 0;// Index of the current song in the list

    /**
     * Creates a MediaPlayer object for a certain song
     * 
     * @param song name of the song
     * @return MediaPlayer for the song
     */
    private MediaPlayer createPlayer(String song) {
        /* Find the file and create MediaPlayer */
        MediaPlayer player = null;
        String audioFilePath = getClass().getResource(BASE_URL + song).toExternalForm();
        Media media = new Media(audioFilePath);
        player = new MediaPlayer(media);
        /*
         * Set what happens when the song finishes: a new player is created and the next
         * song is played
         */
        player.setOnEndOfMedia(() -> {

            if (this.currentIndex < this.songs.size() - 1) {
                this.currentIndex++;
            } else {
                this.currentIndex = 0;
            }
            this.player.stop();
            this.player.dispose();
            this.player = this.createPlayer(this.songs.get(this.currentIndex));
            this.player.play();
        });

        return player;
    }

    /**
     * Creates a Music Player with a given list of songs
     * 
     * @param songs initial list of songs' names
     */
    public MusicPlayer(List<String> songs) {
        this.songs = songs;
    }

    /**
     * Creates a player for the first song of the list
     */
    public void start() {
        this.player = this.createPlayer(this.songs.getFirst());
    }

    /*
     * Stops the music, disposes the music player and exits the app that allows
     * playing music
     */
    public void stop() {
        this.player.stop();
        this.player.dispose();
        Platform.exit();
    }

    /**
     * @param fromSong name of the song of which you want to know the next one
     * @return name of the next song; it will be the first one if the current one is
     *         the last song. null is returned if the song can not be found
     */
    public String getnextSong(String fromSong) {
        int songIndex = this.songs.indexOf(fromSong);

        if (songIndex == -1)
            return null;

        return this.songs.get((songIndex + 1) % this.songs.size());
    }

    /**
     * @param fromSong name of the song of which you want to know the previous one
     * @return name of the previous song; it will be the last one if the current one
     *         is the first song. null is returned if the song can not be found
     */
    public String getPreviousSong(String fromSong) {
        int songIndex = this.songs.indexOf(fromSong);

        if (songIndex == -1) {
            return null;
        }

        return this.songs.get(((songIndex - 1) + this.songs.size()) % this.songs.size());
    }

    /**
     * Adds new song to the list
     * 
     * @param song name of the song
     */
    public void addSong(String song) {
        this.songs.add(song);
    }

    /**
     * Plays the current song
     */
    public void play() {
        this.player.play();
    }

    /**
     * Pauses the current song
     */
    public void pause() {
        this.player.pause();
    }

    /**
     * Updates the status of the music player
     * 
     * @param newPlayerStatus status to update to
     */
    public void update(PlayerStatus newPlayerStatus) {
        /* Get information about the new status */
        SongInstant songInstant = newPlayerStatus.getInstant();
        String song = songInstant.getSong();
        Duration instant = songInstant.getInstant();
        int songIndex = this.songs.indexOf(song);
        Status status = newPlayerStatus.getStatus();

        /* Update to new status */
        this.currentIndex = songIndex;
        this.player.stop();
        this.player.dispose();
        this.player = createPlayer(song);
        this.player.setOnReady(() -> {
            this.player.seek(instant);
            status.set(this);
        });
        this.currentIndex = songIndex;
    }

    /**
     * @return status (playing or paused) of the music
     */
    public Status getStatus() {
        if (this.player == null) {
            return Status.PAUSED;
        }

        /* Get the status of the MediPlayer */
        MediaPlayer.Status playerStatus = this.player.getStatus();
        int attemps = 5;

        /*
         * In case the player is being initialized, we wait until is either playing or
         * paused
         */
        while (playerStatus != MediaPlayer.Status.PLAYING && playerStatus != MediaPlayer.Status.PAUSED
                || attemps < 0) {
            playerStatus = this.player.getStatus();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            attemps--;
        }

        /* If we don't get PLAYING or PAUSED, we will consider it's PAUSED */
        if (attemps < 0) {
            return Status.PAUSED;
        }

        /* Return new status */
        return Status.transform(playerStatus);
    }

    /**
     * Returns the song and the instant that will be played at a certain moment
     * assuming no changes since a certain moment
     * 
     * @param fromSongInstant song and instant originally being played
     * @param fromTime        time when fromSongInstant is being played
     * @param targetTime      time we want to know information about
     * @return SongInstant object containing the mentioned information
     */
    public SongInstant getSongInstantFrom(SongInstant fromSongInstant, long fromTime, long targetTime) {
        /* Time passed since the previous moment */
        long timeLeft = targetTime - fromTime;

        /* Get total duration of the song played previously */
        String song = fromSongInstant.getSong();
        String audioFilePath = getClass().getResource(BASE_URL + song).toExternalForm();
        Media media = new Media(audioFilePath);
        Duration songDuration = media.getDuration();

        /* Get the new instant at where the player will be positioned at the new time */
        Duration newDuration = fromSongInstant.getInstant().add(Duration.millis(timeLeft));

        /*
         * If the new instant is greater than the total duration, it means that the next
         * song will start
         */
        while (newDuration.greaterThan(songDuration)) {
            int songIndex = this.songs.indexOf(song);

            if (songIndex == -1) {
                return null;
            }

            /* The song played will be the next one in the list */
            if (songIndex == this.songs.size() - 1) {
                songIndex = 0;
            } else {
                songIndex++;
            }

            song = this.songs.get(songIndex);

            /* The instant at where the player will be */
            newDuration = newDuration.subtract(songDuration);

            /*
             * Get the duartion of the new song to check if new duration is still greater
             * than the total duration
             */
            audioFilePath = getClass().getResource(BASE_URL + song).toExternalForm();
            media = new Media(audioFilePath);
            songDuration = media.getDuration();
        }

        return new SongInstant(song, newDuration);
    }

    /**
     * Returns the song and the instant that will be played at a certain moment
     * assuming no changes since now
     * 
     * @param targetTime time we want to know information about
     * @return SongInstant object containing the mentioned information
     */
    public SongInstant getSongInstantFromNow(long targetTime) {
        String song = this.songs.get(currentIndex);

        /* Wait until player is ready (if needed) */
        while (this.player == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return new SongInstant(songs.getFirst(), Duration.ZERO);
            }
        }

        Duration currentDuration = this.player.getCurrentTime();
        long currentTime = Instant.now().toEpochMilli();
        SongInstant currentInstant = new SongInstant(song, currentDuration);

        // If they player is paused, we will remain in the same instant.
        if (this.getStatus() == Status.PAUSED) {
            return currentInstant;
        }

        return this.getSongInstantFrom(currentInstant, currentTime, targetTime);
    }

}
