package music_player;

import utils.SongInstant;

public class PlayerStatus {
    private SongInstant instant;
    private Status status;
    
    public PlayerStatus(SongInstant instant, Status status) {
        this.instant = instant;
        this.status = status;
    }

    public SongInstant getInstant() {
        return instant;
    }

    public Status getStatus() {
        return status;
    }

    
}
