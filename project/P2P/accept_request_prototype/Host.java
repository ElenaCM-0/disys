package P2P.accept_request_prototype;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import P2P.accept_request_prototype.Connection;


//class for the host

public class Host {
    private int port;

    public Host(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(
                "Host is listening to the port " + port
                );


            while (true) {
                // waits for incoming connections
                Socket socket = serverSocket.accept();
                System.out.println(
                    "New node connected: " + socket.getInetAddress().getHostAddress()
                    );

                // this creates a new connection object for each node
                Connection connection = new Connection("nodeID", true, socket);

                //this starts the connection in a new thread
                Thread connectionThread = new Thread(connection);
                connectionThread.start();
            }
        }
        catch (IOException e) {
            System.out.println(
                "Error starting host server: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    //starting the host with example port number
    public static void main(String[] args) {
        Host host = new Host(12345);
        host.start();
    }
}