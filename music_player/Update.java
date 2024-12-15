package music_player;

import org.json.JSONObject;

import javafx.util.Duration;
import utils.SongInstant;

public class Update {
    private Status status;
    private long executionTime;
    private SongInstant songInstant;
    private int id;
    private static int num_updates = 0;

    public Update(Status status, long executionTime, String songName, Duration songTime) {
        this(new SongInstant(songName, songTime), status, executionTime);
    }

    public Update(PlayerStatus playerStatus, Long executionTime) {
        this(playerStatus.getInstant(), playerStatus.getStatus(), executionTime);
    }

    private Update(SongInstant instant, Status status, Long executionTime) {
        this.status = status;
        this.executionTime = executionTime;
        this.songInstant = instant;
        id = num_updates;
        num_updates++;
    }

    public Status getStatus() {
        return status;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public SongInstant geSongInstant() {
        return songInstant;
    }

    public String getSongName() {
        return songInstant.getSong();
    }

    public Duration getSongTime() {
        return songInstant.getInstant();
    }

    /**
     * @return This method creates a JSON object containing all the information
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