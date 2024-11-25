package utils;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import org.json.*;

public class MySocket {
    private Socket tunnel;
    private OutputStreamWriter out;
    private BufferedReader in;

    /**
     * It will create the channels for socket connection
     * @param socket An already created socket
     */
    public MySocket(Socket socket) throws UnknownHostException, IOException{
        this.tunnel = socket;
        try {
            out = new OutputStreamWriter(tunnel.getOutputStream(), StandardCharsets.UTF_8);
            in = new BufferedReader(new InputStreamReader(tunnel.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }
    }

    /**
     * It will create the socket connection
     * @param ip The ip address the socket will be connected to
     * @param port The port the socket will connect to
     */
    public MySocket(String ip, int port) throws UnknownHostException, IOException{
        this(new Socket(ip, port));
    }

    /**
     * Sends a message through the socket
     * @param message JSON message that is to be sent
     * @throws IOException
     */
    public void send(JSONObject message) throws IOException {
        out.write(message.toString() + "\n");
        out.flush();
    }

    /**
     * Receives a message through the socket
     * A call to this is blocking. It will wait until a message is received
     * 
     * @return The JSON object that was received
     * @throws IOException
     */
    public JSONObject receive() throws IOException{
        String messageStr;
        
        messageStr = in.readLine();
                
        if (messageStr == null) {

            return null;
        }

        return new JSONObject(messageStr);
    }
}
