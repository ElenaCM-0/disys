package exceptions;

public class ExceededTimeException extends MusicPlayerException {
    public ExceededTimeException(String song, double seconds) {
        super("You can not go  " + seconds + " seconds forward in the song " + song
                + "because it exceeds the duration of the song");
    }
}
