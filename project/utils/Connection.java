package utils;

import java.io.IOException;
import java.net.*;

import org.json.JSONObject;

public class Connection implements Runnable {
    protected String peer;
    protected MySocket socket;

    public Connection(String peer, String ip, int port) throws UnknownHostException, IOException {
        socket = new MySocket(ip, port);
        this.peer = peer;
    }

    public Connection(String peer, Socket sock) throws UnknownHostException, IOException {
        socket = new MySocket(sock);
        this.peer = peer;
    }

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
     * When this method is called, a new thread will listen in on the messages sent
     * through this connection,
     * dealing with them as they are received
     */
    @Override
    public void run() {
        JSONObject message;

        try {
            while ((message = socket.receive()) != null) {
                switch (message.getString("type")) {
                    case "request_message": {

                        /* Show the message to the user */
                    }
                    default: {
                        /* Malformed message, discard */
                        continue;
                    }
                }

            }

            return;
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }

    }

    public String getPeer() {
        return peer;
    }

    public MySocket getSocket() {
        return socket;
    }

}
