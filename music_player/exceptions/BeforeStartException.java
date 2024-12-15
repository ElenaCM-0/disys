package music_player.exceptions;

public class BeforeStartException extends MusicPlayerException {
    public BeforeStartException(String song, double seconds) {
        super("You can not go  " + seconds + " ms back in the song " + song
                + "because the new time would be before the begining of the song");
    }
}
