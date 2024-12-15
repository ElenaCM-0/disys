package utils;

import javafx.util.Duration;

/**
 * Class that represents a song and a certain moment of that song.
 */
public class SongInstant {
    private String song; // Name of the song
    private Duration instant; // Instant of a song

    public SongInstant(String song, Duration instant) {
        this.song = song;
        this.instant = instant;
    }

    /**
     * @return name of the song
     */
    public String getSong() {
        return song;
    }

    /**
     * @return instant of the song
     */
    public Duration getInstant() {
        return instant;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((song == null) ? 0 : song.hashCode());
        result = prime * result + ((instant == null) ? 0 : instant.hashCode());
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
        SongInstant other = (SongInstant) obj;
        if (song == null) {
            if (other.song != null)
                return false;
        } else if (!song.equals(other.song))
            return false;
        if (instant == null) {
            if (other.instant != null)
                return false;

        } else if (Math.abs(instant.toSeconds() - other.instant.toSeconds()) > 2)
            return false;
        return true;
    }

}
