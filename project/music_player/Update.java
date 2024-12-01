package music_player;

import org.json.JSONObject;

public class Update {
    private Status status;
    private long executionTime;
    private String songName;
    private long songTime;
    private static int num_updates = 0;

    public Update(Status status, long executionTime, String songName, long songTime) {
        this.status = status;
        this.executionTime = executionTime;
        this.songName = songName;
        this.songTime = songTime;
        num_updates++;
    }

    public Status getStatus() {
        return status;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getSongName() {
        return songName;
    }

    public long getSongTime() {
        return songTime;
    }

    /**
     * @return This method creates a JSON object containing all the information about the update
     */
    public JSONObject createUpdateJSON() {
        JSONObject ret = new JSONObject();

        ret.put("action", status.toString());
        ret.put("action_timestamp", executionTime);
        ret.put("song_name", songName);
        ret.put("song_time", songTime);
        ret.put("total_updates", num_updates - 1);

        return ret;
    }

}