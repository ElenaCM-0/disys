package music_player;

import org.json.JSONObject;

import javafx.util.Duration;
import utils.SongInstant;

public class Update {
    private Status status;
    private long executionTime;
    private SongInstant songInstant;
    private static int num_updates = 0;

    public Update(Status status, long executionTime, String songName, Duration songTime) {
        this.status = status;
        this.executionTime = executionTime;
        this.songInstant = new SongInstant(songName, songTime);
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
        ret.put("song_time", getSongTime());
        ret.put("total_updates", num_updates - 1);

        return ret;
    }

}