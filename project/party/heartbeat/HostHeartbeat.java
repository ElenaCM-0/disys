package party.heartbeat;

import java.time.Instant;

import main.Main;
import music_player.MusicPlayer;

public class HostHeartbeat extends Heartbeat{

    @Override
    protected void adjustHeartbeat() {
        Main main = Main.getInstance();
        Timesmain.getMusicPlayer().getPosition(main.getNearestChange());
    }

    @Override
    protected long MAX_DISTANCE() {return 5;}
    
    @Override
    protected int SLEEP_SEC() {return 25;}

}
