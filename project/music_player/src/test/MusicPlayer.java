package test;

import javafx.util.Duration;

import java.nio.file.Paths;
import java.util.List;

import exceptions.ExceededTimeException;
import exceptions.NoMoreSongsException;
import exceptions.SongNotFoundException;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicPlayer {
    private static final String BASE_URL = "/songs/";
    private List<String> songs;
    private MediaPlayer player;
    private int index = 0;

    /**
     * Creates a MediaPlayer object for a certain song
     * 
     * @param song name of the song
     * @return MediaPlayer for the song
     * @throws SongNotFoundException if the song can not be found
     */
    private MediaPlayer createPlayer(String song) throws SongNotFoundException {
        MediaPlayer player;
        try {
            String audioFilePath = getClass().getResource(BASE_URL + song).toExternalForm();
            Media media = new Media(audioFilePath);
            player = new MediaPlayer(media);
            player.setOnEndOfMedia(() -> {
                if (this.index < this.songs.size() - 1) {
                    // Create player for next song and start playing it
                    this.index++;
                    this.player = this.createPlayer(this.songs.get(this.index));
                    this.player.play();
                }
            });
        } catch (Exception e) {
            throw new SongNotFoundException(songs.getFirst());
        }

        return player;
    }

    /**
     * Constructor. Initializes a player for the first song.
     * 
     * @param songs initial list of songs' names
     * @throws SongNotFoundException if the first song of the list can not be found
     */
    public MusicPlayer(List<String> songs) throws SongNotFoundException {
        this.songs = songs;
        this.player = createPlayer(songs.getFirst());
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
     * +
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
     * Advances a certain number of seconds in a song
     * 
     * @param seconds number of seconds to advamce
     * @throws ExceededTimeException if the actual time plus the number of seconds
     *                               given exceeds the duration of the song
     */
    public void advance(double seconds) throws ExceededTimeException {
        Duration currentTime = this.player.getCurrentTime();
        Duration newTime = currentTime.add(Duration.seconds(seconds));
        if (newTime.greaterThan(this.player.getTotalDuration())) {
            throw new ExceededTimeException(this.getCurrentSong(),
                    seconds);
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
        this.index++;
        String newSong = "";
        try {
            newSong = this.songs.get(index);
            this.player.stop();
            this.player.dispose();
            this.player = createPlayer(newSong);
            this.player.play();
        } catch (IndexOutOfBoundsException e1) {
            this.index--;
            throw new NoMoreSongsException();
        } catch (Exception e2) {
            this.index--;
            this.player = createPlayer(songs.get(index));
            throw new SongNotFoundException(newSong);
        }
    }

    /**
     * Plays the previous song from the beginning
     * 
     * @throws SongNotFoundException if the song can not be found
     */
    public void previousSong() throws SongNotFoundException {
        this.index--;
        String newSong = "";
        try {
            newSong = this.songs.get(index);
            this.player.stop();
            this.player.dispose();
            this.player = createPlayer(newSong);
            this.player.play();
        } catch (Exception e) {
            this.index++;
            this.player = createPlayer(songs.get(index));
            throw new SongNotFoundException(newSong);
        }
    }
}
