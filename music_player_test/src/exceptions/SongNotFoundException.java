package exceptions;

public class SongNotFoundException extends MusicPlayerException {
    public SongNotFoundException(String song) {
        super("The song " + song + " could not be found");
    }

}
