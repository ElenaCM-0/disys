package utils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import org.json.*;

/**
 * Class that represents a customized socket, used by all connections to
 * communicate
 */
public class MySocket {
    private Socket tunnel; // Real socket
    private OutputStreamWriter out; // Writer for the socket
    private BufferedReader in; // Reader for the socket

    private final int TIMEOUT = 5000; // Timeout that will be set to the socket so that it doesn't wait for messages
                                      // forever

    /**
     * It will create the channels for socket connection
     * 
     * @param socket An already created socket
     */
    public MySocket(Socket socket) throws UnknownHostException, IOException {
        this.tunnel = socket;
        this.tunnel.setSoTimeout(TIMEOUT);
        try {
            out = new OutputStreamWriter(tunnel.getOutputStream(), StandardCharsets.UTF_8);
            in = new BufferedReader(new InputStreamReader(tunnel.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Constructor that uses ip and port to create a new socket
     * 
     * @param ip   The ip address the socket will be connected to
     * @param port The port the socket will connect to
     */
    public MySocket(String ip, int port) throws UnknownHostException, IOException {
        this(new Socket(ip, port));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tunnel == null) ? 0 : tunnel.hashCode());
        result = prime * result + ((out == null) ? 0 : out.hashCode());
        result = prime * result + ((in == null) ? 0 : in.hashCode());
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
        MySocket other = (MySocket) obj;
        if (tunnel == null) {
            if (other.tunnel != null)
                return false;
        } else if (!tunnel.equals(other.tunnel))
            return false;
        if (out == null) {
            if (other.out != null)
                return false;
        } else if (!out.equals(other.out))
            return false;
        if (in == null) {
            if (other.in != null)
                return false;
        } else if (!in.equals(other.in))
            return false;
        return true;
    }

    /**
     * Sends a message through the socket
     * 
     * @param message JSON message that is to be sent
     * @throws IOException
     */
    public void send(JSONObject message) throws IOException {
        out.write(message.toString() + "\n");
        out.flush();
    }

    /**
     * Receives a message through the socket
     * A call to this is blocking. It will wait until a message is received, except
     * if the thread is interrupted
     * 
     * @return The JSON object that was received
     * @throws IOException
     * @throws InterruptedException
     */
    public JSONObject receive() throws IOException, InterruptedException {
        String messageStr;

        while (true) {
            try {
                messageStr = in.readLine();

                if (messageStr == null) {

                    return null;
                }

                return new JSONObject(messageStr);
            } catch (IOException e) {
                // If timeout is reached, and the thread is interrumpted, the function returns.
                // Uf not it will continue receiving messages
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
            }
        }

    }

    /**
     * Receives a message of the given type through the socket. It will discard all
     * other messages
     * A call to this is blocking. It will wait until a message is received, except
     * if the thread is interrupted
     * 
     * @param type The message type that the user expects to receive
     * 
     * @return The JSON object that was received
     * @throws IOException
     * @throws InterruptedException
     */
    public JSONObject receive(String type) throws IOException, InterruptedException {
        JSONObject message;

        while (true) {
            message = this.receive();
            if (message == null)
                return null;
            if (message.getString("type").equals(type))
                return message;
        }
    }

    /**
     * Closes the socket
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        this.tunnel.close();
    }

    /**
     * @return if the socket is closed or not
     */
    public boolean isClosed() {
        return this.tunnel.isClosed();
    }

    /**
     * Set the socket timeout to a certain value
     * 
     * @param timeout timeout to set
     * @throws SocketException
     */
    public void setSoTimeout(int timeout) throws SocketException {
        this.tunnel.setSoTimeout(timeout);
    }

}
