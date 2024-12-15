package music_player;

import java.util.function.Consumer;

import javafx.scene.media.MediaPlayer;

/**
 * Enumeration that represents the status of the music, and contains the
 * function to set a music player a certain status
 */
public enum Status {
    PLAYING("playing", mp -> mp.play()), PAUSED("paused", mp -> mp.pause());

    private String status; // String with the name of the status
    private Consumer<MusicPlayer> setFunction; // Function that sets a music player to a certain status

    private Status(String status, Consumer<MusicPlayer> setFunction) {
        this.status = status;
        this.setFunction = setFunction;
    }

    @Override
    public String toString() {
        return this.status;
    }

    /**
     * Changes the status of a music player to the one represented by the current
     * object
     * 
     * @param mp MusicPlayer object whose status will be changed
     */
    public void set(MusicPlayer mp) {
        this.setFunction.accept(mp);
    }

    /**
     * Method that compares the given string with the possible status
     * 
     * @param status String to compare the enum elements to
     * @return the enum element the string matches or null if it matches no elements
     */
    public static Status match(String status) {
        for (Status s : Status.values()) {
            if ((s.status).equals(status))
                return s;
        }

        return null;
    }

    /**
     * Transform a status from MediaPlayer.Status class to an object of this class
     * 
     * @param st status from other class
     * @return stqtus of this class, corresponding to the original one
     */
    public static Status transform(MediaPlayer.Status st) {
        if (st == MediaPlayer.Status.PLAYING) {
            return Status.PLAYING;
        }
        return Status.PAUSED;
    }
}
