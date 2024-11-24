package P2P;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class connection implements Runnable {
    private Socket tunnel; // get the output stream from the socket.
    ObjectOutputStream out;
    ObjectInputStream in;

    public connection(String ip, int port) throws UnknownHostException, IOException{
        tunnel = new Socket(ip, port);
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(tunnel.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }


    }
    
}
