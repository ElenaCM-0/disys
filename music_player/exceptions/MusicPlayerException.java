package music_player.exceptions;

public class MusicPlayerException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    protected String message;

    /**
     * Constructor
     * 
     * @param message Stringb that will be printed on screen if the exception is
     *                thrown
     */
    public MusicPlayerException(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }

}
