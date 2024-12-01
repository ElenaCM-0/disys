package music_player.exceptions;

import javafx.scene.media.MediaPlayer;

public class FatalMPError extends MusicPlayerException {
    public FatalMPError(MediaPlayer.Status status) {
        super("Something went really wrong with the music player. It is in status " + status);
    }
}
