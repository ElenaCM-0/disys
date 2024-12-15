package music_player.exceptions;

public class NoMoreSongsException extends MusicPlayerException {
    public NoMoreSongsException() {
        super("You can not skip to the next songs because there are no more songs available");
    }

}
