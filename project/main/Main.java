package main;

import utils.Connection;
import utils.MessageType;
import utils.MySocket;
import utils.SharedInfo;

import music_player.MusicPlayerThread;
import music_player.Update;
import netscape.javascript.JSObject;
import p2p.P2PConnection;
import party.Action;
import party.PartyConnection;
import party.heartbeat.Heartbeat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.*;
import org.json.JSONObject;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static Main instance = null;
    private Heartbeat heartbeat;
    private List<Connection> listConnections;
    private List<String> availableSongs;
    private boolean delayedHeartbeat = false;
    private boolean host;
    private MusicPlayerThread musicPlayerThread;
    private Scanner scanner = new Scanner(System.in);
    private PartyConnection partyConnection; /*
                                              * If you are the host, this will be a hostConnection, however, if you are
                                              * a playing party member, this will be the connection that connects you to
                                              * the host
                                              */

    private SharedInfo partyRequests = new SharedInfo();
    private SharedInfo partyAnswers = new SharedInfo();
    private Map<Connection, Thread> connectionThreads = new HashMap<>();
    private static final int PORT = 1234;

    public static Main getInstance() {
        if (instance == null)
            instance = new Main();

        return instance;
    }

    /*******************************************************************************************
     * USER INTERACTION
     *******************************************************************************************/
    public static void main(String[] args) throws UnknownHostException, IOException {
        Main main = new Main();

        // implement here part to connect to a party or start a party
        main.p2pmenu();

        // Here disconnect form network part
    }

    private void p2pmenu() throws UnknownHostException, IOException {
        // configuration of the net:
        InetAddress localHost = InetAddress.getLocalHost();
        System.out.print("Your IP address is: " + localHost + " Share it with one of the nodes."); // how do we control
                                                                                                   // which one?
        System.out.print("Write the IP address of the node next to you: ");
        String ipNeighbour = scanner.nextLine();
        System.out.print("Write the user name of the node next to you: ");
        String userNeighbour = scanner.nextLine();
        ServerSocket serverSocket = new ServerSocket(PORT);
        MySocket mysocket1 = new MySocket(ipNeighbour, PORT);
        P2PConnection con1 = new P2PConnection(userNeighbour, mysocket1);
        Socket socket2 = serverSocket.accept(); // accept the peer who is trying to connect
        MySocket mysocket2 = new MySocket(socket2);
        JSONObject userNeighbourJson = mysocket2.receive();
        String userNeighbour2=userNeighbourJson.getString("user");
        P2PConnection con2 = new P2PConnection(userNeighbour2, mysocket2);
        con1.run();
        con2.run();

        Boolean exit = false;

        while (!exit) {
            // shows options to start or join party
            System.out.println("You are not currently in a party");
            System.out.println("Type 'party' to start a new party or wait for an invitation to join an existing party");
            String input = scanner.nextLine();

            if (this.partyAnswers.getWaitingConnection() != null) {
                boolean yes = receiveYN(input);
                partyAnswers.setWaitingConnection(null);
                partyAnswers.setAnswer(yes);
                if (yes) {
                    joinParty();
                    // Connection waitingConnection = partyAnswers.getWaitingConnection();
                    // for (Connection c : this.connectionThreads.keySet()) {
                    // if (!c.equals(waitingConnection)) {
                    // connectionThreads.get(c).interrupt();
                    // }
                    // }
                    // this.partyConnection = new MemberConnection(waitingConnection);
                    // this.playingPartyMenu();
                }

            } else if (input.equals("party")) {
                startParty();
            } else {
                System.out.println("Invalid command \"" + input + "\"");
            }

        }
    }

    private void joinParty() {
        // TODO Do all necessary things to join a party
        /*
         * -Close useless connections
         * -Create a member connection and execute it
         * -Create a music player and a music player thread and execute
         * -Create heartbeat thread
         */
    }

    /**
     * This method will do all the necessary preparations for a user to create a
     * playing party
     * it should be called if the user selects "party" in the p2pmenu
     */
    private void startParty() {
        System.out.println("Starting party...");

        System.out.println("First, you must select the songs you would like to play in the playing party");
        List<String> partySongs = askUserSongs();

        /* Send the request to the other nodes */

        JSONObject request = new JSONObject();

        request.put("type", MessageType.PARTY_REQUEST);

        try {
            sendToAllConnections(request);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * This method will be called when the node is in a playing party, it will print
     * out the options that the user has and it will process the user's input.
     * This methdo will handle the situation where the user is disconnected from the
     * host
     */
    private void playingPartyMenu() {
        String action;

        while (true) {
            System.out.println("You are in a party! You can use either of these commands:"
                    + "- play: if you want to play the music"
                    + "- pause: if you want to stop the song"
                    + "- forward: if you want to skip to the next song"
                    + "- backward: if you want to go back to the previous song"
                    + "- exit: if you want to disconnect from the playing party"
                    + "Note: if your request is not posible to execute (f.e you skip and it is the last song), your request will be ignored");

            action = scanner.nextLine();

            if (delayedHeartbeat) {
                if (!receiveYN(action))
                    return;

                continue;
            }
            Action matchedAction = Action.match(action);

            if (matchedAction == null) {
                if (action.equalsIgnoreCase("Exit"))
                    return;

                System.out.print("The action you entered is not one of the available options");
                continue;
            }

            try {
                partyConnection.sendActionRequest(matchedAction);
            } catch (IOException e) {
                System.out.println("You action cannot be executed");
                continue;
            }
        }

    }

    /**
     * This method will process the user input, only accepting answers that can be
     * interpreted as yes or no
     * If the user's answer is not within the options, this method will ask again
     * 
     * @param answer The current answer the user has given
     * @return true if the user said 'Yes', false if the user said 'No'
     */
    private boolean receiveYN(String answer) {
        while (true) {
            switch (answer) {
                case "Y":
                case "Yes":
                case "y":
                case "yes":
                    return true;
                case "N":
                case "No":
                case "n":
                case "no":
                    return false;
            }

            System.out.println("Error, please write \'yes\' or \'no\'");

            answer = scanner.nextLine();
        }
    }

    /**
     * Asks the user to select songs from a list of available songs and adds them to
     * a party playlist.
     * The method displays all available songs, allows the user to input song names
     * to add to the playlist,
     * and continues until the user types "done".
     *
     * Validations:
     * - If the song is not in the list of available songs, the user is informed
     * that it is not available.
     * - If the song is already in the party playlist, the user is informed that it
     * has already been added.
     *
     * @return A list of strings representing the songs selected by the user for the
     *         party playlist.
     */
    public List<String> askUserSongs() {
        List<String> partySongs = new ArrayList<>();
        System.out.println("These are the available songs: ");
        for (String song : availableSongs) {
            System.out.println("- " + song);
        }
        System.out.println("\nType the name of the song you want to add to the party.");
        System.out.println("When you're done, type 'done'.\n");
        while (true) {
            System.out.print("Enter a song: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("done")) {
                break;
            } else if (availableSongs.contains(input)) {
                if (!partySongs.contains(input)) {
                    partySongs.add(input);
                    System.out.println(input + " has been added to the party!");
                } else {
                    System.out.println(input + " is already in the party list.");
                }
            } else {
                System.out.println(input + " is not an available song.");
            }
        }
        return partySongs;
    }

    /*******************************************************************************************
     * Connection-related
     *******************************************************************************************/

    /**
     * This method will change the variable in main showing that the heartbeat
     * thread has not heard form the host in too long
     */
    public void notHeardFromHost() {
        delayedHeartbeat = true;

        System.out.println("Disconnected from the host, continue playing party? (Y/N)");
    }

    /**
     * Method that sends the given JSONObject to all the connections that the main
     * has
     * 
     * @param message The object to be sent through the connections
     * @throws IOException
     */
    public void sendToAllConnections(JSONObject message) throws IOException {
        for (Connection con : listConnections) {
            con.send(message);
        }
    }

    /**
     * @return the amount of seconds ahead of the current time an action has to be
     *         for it to have time to reach all of the nodes and for the nodes to
     *         have time to take all of
     *         the actions
     */
    private int getMilisec() {
        /** TODO **/
    }

    /**
     * This method will return the timestamp when the nearest change can be
     * implemented
     * 
     * @return the timestamp when the nearest change can be implemented
     */
    public long getNearestChange() {
        return Instant.now().getEpochSecond() + getMilisec();
    }

    /*******************************************************************************************
     * getters/setters
     *******************************************************************************************/

    public MusicPlayerThread getMusicPlayerThread() {
        return musicPlayerThread;
    }

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

}
