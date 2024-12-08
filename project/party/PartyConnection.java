package party;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import utils.Connection;
import utils.MySocket;

public abstract class PartyConnection extends Connection{

    public PartyConnection(String peer, String ip, int port) throws UnknownHostException, IOException {
        super(peer, ip, port);
    }

    public PartyConnection(String peer, Socket sock) throws UnknownHostException, IOException {
        super(peer, sock);
    }

    public PartyConnection(String peer, MySocket socket) throws UnknownHostException, IOException {
        super(peer, socket);
    }

    public abstract void sendActionRequest(Action act) throws IOException;
}