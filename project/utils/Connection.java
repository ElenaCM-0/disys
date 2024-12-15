package utils;

import java.io.IOException;
import java.net.*;

import org.json.JSONObject;

/**
 * Class that represents the general characteristics of any kind of connection
 * used by the app
 */
public abstract class Connection implements Runnable {
    protected String peer; // Name or id os the peer to which this node is connected through this
                           // connection
    protected MySocket socket; // Socket that connects a node with another

    /**
     * Creates a socket with indicated ip and port and assigns it to a
     * connection
     * 
     * @param peer name or id of the peer corresponding to the ip and port
     * @param ip   ip to connect to
     * @param port port to connect to
     * @throws UnknownHostException
     * @throws IOException
     */
    public Connection(String peer, String ip, int port) throws UnknownHostException, IOException {
        socket = new MySocket(ip, port);
        this.peer = peer;
    }

    /**
     * Creates a MySocket object with an already existent socket and assigns it to a
     * connection
     * 
     * @param peer name or id of the peer to which the socket is connected
     * @param sock already initialized socket
     * @throws UnknownHostException
     * @throws IOException
     */
    public Connection(String peer, Socket sock) throws UnknownHostException, IOException {
        socket = new MySocket(sock);
        this.peer = peer;
    }

    /**
     * Assigns socket and peer to a new connection
     * 
     * @param peer name or id of the peer to which the socket is connected
     * @param sock already initialized socket
     * @throws UnknownHostException
     * @throws IOException
     */
    protected Connection(String peer, MySocket sock) throws UnknownHostException, IOException {
        socket = sock;
        this.peer = peer;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((peer == null) ? 0 : peer.hashCode());
        result = prime * result + ((socket == null) ? 0 : socket.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Connection other = (Connection) obj;
        if (peer == null) {
            if (other.peer != null)
                return false;
        } else if (!peer.equals(other.peer))
            return false;
        if (socket == null) {
            if (other.socket != null)
                return false;
        } else if (!socket.equals(other.socket))
            return false;
        return true;
    }

    /**
     * Method that sends the given message through the connection
     * 
     * @throws IOException
     */
    public void send(JSONObject message) throws IOException {
        socket.send(message);
    }

    /**
     * @return name or id of the peer
     */
    public String getPeer() {
        return peer;
    }

    /**
     * @return socket corresponding to this connection
     */
    public MySocket getSocket() {
        return socket;
    }

    /**
     * Closes the socket corresponding to this connection
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        this.socket.close();
    }

    /**
     * @return true if the socket corresponding to this connection is closed, false
     *         otherwise
     */
    public boolean isClosed() {
        return this.socket.isClosed();
    }

}
