package music_player;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import music_player.exceptions.BeforeStartException;
import music_player.exceptions.ExceededTimeException;
import music_player.exceptions.FatalMPError;
import music_player.exceptions.NoMoreSongsException;
import music_player.exceptions.SongNotFoundException;
import utils.SongInstant;

public class MusicPlayer {
    private static final String BASE_URL = "../songs/";
    private List<String> songs;
    private MediaPlayer player;
    private int currentIndex = 0;

    /**
     * Creates a MediaPlayer object for a certain song
     * 
     * @param song name of the song
     * @return MediaPlayer for the song
     * @throws SongNotFoundException if the song can not be found
     */
    private MediaPlayer createPlayer(String song) throws SongNotFoundException {
        MediaPlayer player = null;
        ;
        try {
            String audioFilePath = getClass().getResource(BASE_URL + song).toExternalForm();
            Media media = new Media(audioFilePath);
            player = new MediaPlayer(media);
            player.setOnEndOfMedia(() -> {
                if (this.currentIndex < this.songs.size() - 1) {
                    // Create player for next song and start playing it
                    this.currentIndex++;
                    this.player.stop();
                    this.player.dispose();
                    this.player = this.createPlayer(this.songs.get(this.currentIndex));
                    this.player.play();
                } else {
                    Platform.exit();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return player;
    }

    /**
     * Constructor. Initializes a player for the first song.
     * 
     * @param songs initial list of songs' names
     */
    public MusicPlayer(List<String> songs) {
        this.songs = songs;
    }

    public void start() throws SongNotFoundException {
        this.player = this.createPlayer(this.songs.getFirst());
    }

    public void stop() {
        this.player.stop();
        this.player.dispose();
        Platform.exit();
    }

    /**
     * @return the name of the current Song
     */
    public String getCurrentSong() {
        String source = this.player.getMedia().getSource();
        return Paths.get(source).getFileName().toString();
    }

    /**
     * @return the time of the current song that is being played
     */
    public String getCurrentTime() {
        return this.player.getCurrentTime().toString();
    }

    /**
     * @param fromSong name of the song of which you want to know the next one
     * @return name of the next song or null if the current one is the last song
     */
    public String getnextSong(String fromSong) {
        int songIndex = this.songs.indexOf(fromSong);

        if (songIndex == -1 || songIndex == this.songs.size() - 1) {
            return null;
        }

        return this.songs.get(songIndex + 1);
    }

    /**
     * @param fromSong name of the song of which you want to know the previous one
     * @return name of the previous song or null if the current one is the first
     *         song
     */
    public String getPreviousSong(String fromSong) {
        int songIndex = this.songs.indexOf(fromSong);

        if (songIndex == -1 || songIndex == 0) {
            return null;
        }

        return this.songs.get(songIndex - 1);
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
        SongInstant songInstant = newPlayerStatus.getInstant();
        String song = songInstant.getSong();
        Duration instant = songInstant.getInstant();
        int songIndex = this.songs.indexOf(song);
        Status status = newPlayerStatus.getStatus();

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
     * Advances a certain number of seconds in a song
     * 
     * @param seconds number of seconds to advamce
     * @throws ExceededTimeException if the actual time plus the number of seconds
     *                               given exceeds the duration of the song
     */
    public void advance(double milisecs) throws ExceededTimeException {
        Duration currentTime = this.player.getCurrentTime();
        Duration newTime = currentTime.add(Duration.millis(milisecs));
        if (newTime.greaterThan(this.player.getTotalDuration())) {
            throw new ExceededTimeException(this.getCurrentSong(),
                    milisecs);
        }
        this.player.seek(newTime);
    }

    /**
     * Goes back a certain number of seconds in a song
     * 
     * @param seconds number of seconds to go back
     * @throws BeforeStartException if the actual time minus the number of seconds
     *                              given is less than the initial time of the song
     */
    public void goBack(double milisecs) throws BeforeStartException {
        Duration currentTime = this.player.getCurrentTime();
        Duration newTime = currentTime.subtract(Duration.millis(milisecs));
        if (newTime.lessThan(Duration.ZERO)) {
            throw new BeforeStartException(this.getCurrentSong(),
                    milisecs);
        }
        this.player.seek(newTime);
    }

    /**
     * Plays the next song from the beginning
     * 
     * @throws NoMoreSongsException  if there are no more songs to play
     * @throws SongNotFoundException if the new song can not be found
     */
    public void nextSong() throws NoMoreSongsException, SongNotFoundException {
        this.currentIndex++;
        String newSong = "";
        try {
            newSong = this.songs.get(this.currentIndex);
            this.player.stop();
            this.player.dispose();
            this.player = createPlayer(newSong);
            this.player.play();
        } catch (IndexOutOfBoundsException e1) {
            this.currentIndex--;
            throw new NoMoreSongsException();
        } catch (Exception e2) {
            this.currentIndex--;
            this.player = createPlayer(songs.get(this.currentIndex));
            throw new SongNotFoundException(newSong);
        }
    }

    /**
     * Plays the previous song from the beginning
     * 
     * @throws SongNotFoundException if the song can not be found
     */
    public void previousSong() throws SongNotFoundException {
        this.currentIndex--;
        String newSong = "";
        try {
            newSong = this.songs.get(this.currentIndex);
            this.player.stop();
            this.player.dispose();
            this.player = createPlayer(newSong);
            this.player.play();
        } catch (Exception e) {
            this.currentIndex++;
            this.player = createPlayer(songs.get(this.currentIndex));
            throw new SongNotFoundException(newSong);
        }
    }

    //
    /**
     * @return status (playing or paused) of the player
     */
    public Status getStatus() {
        if (this.player == null) {
            return null;
        }
        
        MediaPlayer.Status playerStatus = this.player.getStatus();
        int attemps = 5;

        // In case the player is being initialized, we wait until is either playing or
        // paused
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

        if (attemps < 0) {
            throw new FatalMPError(playerStatus);
        }

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
        long timeLeft = targetTime - fromTime;

        String song = fromSongInstant.getSong();

        String audioFilePath = getClass().getResource(BASE_URL + song).toExternalForm();
        Media media = new Media(audioFilePath);

        Duration songDuration = media.getDuration();
        Duration newDuration = fromSongInstant.getInstant().add(Duration.millis(timeLeft));

        while (newDuration.greaterThan(songDuration)) {
            int songIndex = this.songs.indexOf(song);

            if (songIndex == -1) {
                // TODO Throw an exception or something
                return null;
            }

            if (songIndex == this.songs.size() - 1) {
                // TODO What should I return if the party will be over
                return null;
            }

            songIndex++;

            song = this.songs.get(songIndex);

            newDuration = newDuration.subtract(songDuration);

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
        
        while (this.player == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
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
