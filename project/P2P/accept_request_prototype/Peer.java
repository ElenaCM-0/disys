package P2P.accept_request_prototype;

import java.io.IOException;
import java.net.Socket;

// class for the node (Peer in this case)

public class Peer {
    private String hostIp;
    private int hostPort;

    public Peer(String hostIp, int hostPort) {
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    public void connectToHost() {
        try {

            // part that connects to the host
            Socket socket = new Socket(hostIp, hostPort);
            System.out.println(
                "Connected to host at " + hostIp + ":" + hostPort
                );

            // this creates a new connection object for communication with the host
            Connection connection = new Connection("peerID", true, socket);
            
            // this starts the connection in a new thread
            Thread connectionThread = new Thread(connection);
            connectionThread.start();

        } 
        catch (IOException e) {
            System.out.println(
                "Error connecting to host: " + e.getMessage()
                );
            e.printStackTrace();    
        }
    }


    // "String[] args" is an array of strings that can be passed
    // to the program from the command line, allowing to provide
    // arguments or parameters when starting the program
    public static void main(String[] args) {
        Peer peer = new Peer("127.0.0.1", 12345); // example host IP and port number
        peer.connectToHost();
    }
}