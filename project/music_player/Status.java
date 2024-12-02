package music_player;

import java.util.function.Consumer;

import javafx.scene.media.MediaPlayer;

public enum Status {
    PLAYING("playing", mp -> mp.play()), PAUSED("paused", mp -> mp.pause());

    private String status;
    private Consumer<MusicPlayer> setFunction;

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
     * @param mp MusicPlayer object which status will be changed
     */
    public void set(MusicPlayer mp) {
        this.setFunction.accept(mp);
    }

    /**
     * Method that compares the given string with the possible actions
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

    public static Status transform(MediaPlayer.Status st) {
        if (st == MediaPlayer.Status.PLAYING) {
            return Status.PLAYING;
        }
        return Status.PAUSED;
    }
}
