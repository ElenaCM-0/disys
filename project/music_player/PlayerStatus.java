package music_player;

import utils.SongInstant;

/**
 * Represents the full status of a music player, includinf the song and instant
 * of the song at where the music player is positioned, and the status of the
 * music
 */
public class PlayerStatus {
    private SongInstant instant; // Name of the song and instant at where the player is positioned
    private Status status; // Status of the music (PLAYING/PAUSED)

    /**
     * Creates a MusicPlayerStatus object with the given parameters
     * 
     * @param instant
     * @param status
     */
    public PlayerStatus(SongInstant instant, Status status) {
        this.instant = instant;
        this.status = status;
    }

    /**
     * @return SongInstant object corresponding to this status
     */
    public SongInstant getInstant() {
        return instant;
    }

    /**
     * @return Status of the music corresponding to this status
     */
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
