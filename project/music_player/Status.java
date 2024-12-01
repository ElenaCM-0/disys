package music_player;

import javafx.scene.media.MediaPlayer;

public enum Status {
    PLAYING("playing"), PAUSED("paused");

    private String status;

    private Status(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.status;
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
