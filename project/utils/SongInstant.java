package utils;

import javafx.util.Duration;

public class SongInstant {
    private String song;
    private Duration instant;

    public SongInstant(String song, Duration instant) {
        this.song = song;
        this.instant = instant;
    }

    public String getSong() {
        return song;
    }

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
        } else if (!instant.equals(other.instant))
            return false;
        return true;
    }

}
