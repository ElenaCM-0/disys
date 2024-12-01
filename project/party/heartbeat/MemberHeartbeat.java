package party.heartbeat;

import java.time.Instant;

public class MemberHeartbeat extends Heartbeat {

    @Override
    protected void adjustHeartbeat() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'adjustHeartbeat'");
    }

    @Override
    protected long MAX_DISTANCE() {return 5;}
    
    @Override
    protected int SLEEP_SEC() {return 30;}
    }
    
}
