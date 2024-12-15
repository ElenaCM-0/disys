package party;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import utils.Connection;
import utils.MessageType;
import org.json.JSONObject;

import main.Main;
import music_player.Update;

/**
 * Class that represents a connection from a member of a party to the host
 */
public class MemberConnection extends Connection {

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
    public MemberConnection(String peer, String ip, int port) throws UnknownHostException, IOException {
        super(peer, ip, port);
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
    public MemberConnection(String peer, Socket sock) throws UnknownHostException, IOException {
        super(peer, sock);
    }

    /**
     * Creates a connectiom with the same properties as another existent connection.
     * No new socket its created, but old socket is used
     * 
     * @param conn connection on which the new connection will be based
     * @throws UnknownHostException
     * @throws IOException
     */
    public MemberConnection(Connection conn) throws UnknownHostException, IOException {
        super(conn.getPeer(), conn.getSocket());
    }

    /**
     * Receives and execites actions requested by the host until it is interrumpted,
     * the socket is closed or an exception occurs. It also updates the time when
     * the last message from the host was received
     */
    @Override
    public void run() {
        JSONObject message;

        try {
            while ((message = socket.receive(MessageType.EXECUTE_ACTION.toString())) != null) {
                Update update = Update.parsefromJSON(message);
                Main main = Main.getInstance();
                main.getMusicPlayerTask().addChange(update);
                main.getHeartbeat().lastUpdate(update.getExecutionTime());
            }

            return;
        } catch (InterruptedException e) {
            return;
        } catch (IOException e) {
            return;
        }

    }

    /**
     * Sends a request to execute an action to the host
     * 
     * @param act action to execute
     * @throws IOException
     */
    public void sendActionRequest(Action act) throws IOException {
        JSONObject message = new JSONObject();
        message.put("type", MessageType.ACTION_REQUEST.toString());
        message.put("command", act.toString());
        this.socket.send(message);
    }
}
