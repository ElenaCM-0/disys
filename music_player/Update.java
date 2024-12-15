package music_player;

import org.json.JSONObject;

import javafx.util.Duration;
import utils.SongInstant;

/**
 * Class that represents an update in a party
 */
public class Update {
    private Status status; // Status of the music (PLAYING or PAUSED)
    private long executionTime; // Time when the changes happen
    private SongInstant songInstant; // Instant of a song at where the music player is positioned
    private int id; // Number that identifies the update
    private static int num_updates = 0; // Keeps track of how many updates have happened

    /**
     * Creates an update with the given parameters
     * 
     * @param status
     * @param executionTime
     * @param songName
     * @param songTime
     */
    public Update(Status status, long executionTime, String songName, Duration songTime) {
        this(new SongInstant(songName, songTime), status, executionTime);
    }

    /**
     * Creates an update with the given parameters
     * 
     * @param playerStatus
     * @param executionTime
     */
    public Update(PlayerStatus playerStatus, Long executionTime) {
        this(playerStatus.getInstant(), playerStatus.getStatus(), executionTime);
    }

    /**
     * Creates an update with the given parameters
     * 
     * @param instant
     * @param status
     * @param executionTime
     */
    private Update(SongInstant instant, Status status, Long executionTime) {
        this.status = status;
        this.executionTime = executionTime;
        this.songInstant = instant;
        id = num_updates;
        num_updates++;
    }

    /**
     * @return the status of the music (PLAYING or PAUSED)
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 
     * @return the time when the changes are executed
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * @return song instant corresponding to the update
     */
    public SongInstant geSongInstant() {
        return songInstant;
    }

    /**
     * @return name of the song corresponding to the update
     */
    public String getSongName() {
        return songInstant.getSong();
    }

    /**
     * 
     * @return instant of the song corresponding to the update
     */
    public Duration getSongTime() {
        return songInstant.getInstant();
    }

    /**
     * @return a JSON object containing all the information
     *         about the update
     */
    public JSONObject createUpdateJSON() {
        JSONObject ret = new JSONObject();

        ret.put("action", status.toString());
        ret.put("action_timestamp", executionTime);
        ret.put("song_name", getSongName());
        ret.put("song_time", getSongTime().toMillis());
        ret.put("total_updates", id - 1);

        return ret;
    }

    /**
     * Gets an update from a JSON object
     * 
     * @param json JSON object containing the information
     * @return a new Update object
     */
    public static Update parsefromJSON(JSONObject json) {
        Status st = Status.match(json.getString("action"));
        long executionTime = json.getLong("action_timestamp");
        String song = json.getString("song_name");
        Duration instant = Duration.millis(json.getDouble("song_time"));
        return new Update(st, executionTime, song, instant);
    }

}