package party;

import java.io.IOException;
import java.net.UnknownHostException;

import utils.Connection;
import utils.MySocket;

public abstract class PartyConnection extends Connection{

    public PartyConnection(String peer, MySocket socket) throws UnknownHostException, IOException {
        super(peer, socket);
    }

    public abstract void sendActionRequest(Action act);
}