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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instant == null) ? 0 : instant.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlayerStatus other = (PlayerStatus) obj;
        if (instant == null) {
            if (other.instant != null)
                return false;
        } else if (!instant.equals(other.instant))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

}
