package party.heartbeat;

import java.time.Instant;

import main.Main;

public class MemberHeartbeat extends Heartbeat {

    @Override
    protected void adjustHeartbeat() {
        Main main = Main.getInstance();

        main.notHeardFromHost();

        repeat = false;
    }

    @Override
    protected long MAX_DISTANCE() {return 5;}
    
    @Override
    protected int SLEEP_SEC() {return 30;}
    }
    
}
