package party.heartbeat;

import main.Main;

public class MemberHeartbeat extends Heartbeat {

    @Override
    protected void adjustHeartbeat() {
        Main main = Main.getInstance();

        main.notHeardFromHost();

        repeat = false;
    }

    @Override
    protected long MAX_DISTANCE() {
        return 10;
    }

    @Override
    protected int SLEEP_SEC() {
        return 35;
    }
}
