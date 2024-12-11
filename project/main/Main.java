package main;

import utils.MessageType;
import utils.MySocket;
import utils.SharedInfo;

import music_player.MusicPlayerTask;
import utils.Connection;
import music_player.MusicPlayer;
import p2p.P2PConnection;
import party.Action;
import party.HostConnection;
import party.heartbeat.Heartbeat;
import party.heartbeat.HostHeartbeat;
import party.heartbeat.MemberHeartbeat;
import party.MemberConnection;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.time.*;
import org.json.JSONObject;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Main {
    private final List<String> availableSongs = List.of("song1", "song2", "song3", "song4");
    private final int TIMEOUT = 15;
    private final int max_party_nodes = 5;

    private static Main instance = null;
    private Thread heartbeatThread;
    private Heartbeat heartbeat;
    private MusicPlayerTask musicPlayerTask;
    private Consumer<Action> sendAction; /*
                                          * If you are the host, this will be a hostConnection, however, if you are
                                          * a playing party member, this will be the connection that connects you to
                                          * the host
                                          */
    private Thread partyConnectionThread;
    private Thread timeout;
    private SharedInfo partyRequests = new SharedInfo();
    private SharedInfo partyAnswers = new SharedInfo();
    private Map<P2PConnection, Thread> connectionThreads = new HashMap<>();
    private static final int PORT = 1234;
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public enum WAKER {
        CON, TIMEOUT, INPUT, HEART;
    }

    public enum MAIN_STATUS {
        P2P, EXIT, JOIN, HOST, PARTY;
    }

    private WAKER waker;
    private MAIN_STATUS status;
    private final Lock talkToMain = new ReentrantLock();
    private Thread userInput;
    private Scanner stdin = null;
    private PrintWriter stdinWriter;
    private PipedOutputStream pipedStdinOutput;
    private boolean requestProcessed;
    private boolean justUser;

    /*
     * private boolean userInput = false;
     * private Boolean host = null;
     * private boolean timeout;
     * private Scanner scanner = new Scanner(System.in);
     */

    private int num_party_nodes;

    /**
     * Function that will return only when requestProcessed is true
     */
    private void waitForMain() {
        while (!requestProcessed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    private Main() throws IOException {
        PipedInputStream pipedStdin = new PipedInputStream();
        pipedStdinOutput = new PipedOutputStream(pipedStdin);
        stdinWriter = new PrintWriter(pipedStdinOutput, true);

        stdin = new Scanner(pipedStdin);

        userInput = new Thread(() -> {
            Scanner stdin_real = new Scanner(System.in);
            String input;
            MAIN_STATUS status_before;
            Boolean yn;

            while ((input = stdin_real.nextLine()) != null) {
                if (justUser) {
                    stdinWriter.println(input);
                    continue;
                }

                status_before = status;
                yn = false;

                try {
                    while (!talkToMain.tryLock(100, TimeUnit.MILLISECONDS)) {
                        if (justUser) {
                            yn = true;
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }

                if (yn)
                    continue;

                if (status_before != status) {
                    /* There have been changes in the main before the input was processed */
                    talkToMain.unlock();
                    continue;
                }

                requestProcessed = false;

                waker = WAKER.INPUT;
                try {
                    queue.put(input);
                } catch (InterruptedException e) {
                    stdin_real.close();
                    return;
                }

                waitForMain();

                talkToMain.unlock();

                if (status == MAIN_STATUS.EXIT) {
                    break;
                }
            }

            stdin_real.close();
        });
    }

    public static Main getInstance() {
        if (instance == null)
            try {
                instance = new Main();
            } catch (IOException e) {
                // TODO
                return null;
            }

        return instance;
    }

    /*******************************************************************************************
     * USER INTERACTION
     * 
     * @throws InterruptedException
     *******************************************************************************************/

    /**
     * Method that will be called by the threads when they want to print something
     * 
     * @param question The string they want to print to the screen
     */
    public void askUser(String question) {
        try {
            queue.put(question);
        } catch (InterruptedException e) {
            return;
        }
    }

    /**
     * Method that will wait on the lock to talk to the main
     */
    public void requestMain() {
        talkToMain.lock();

        requestProcessed = false;

        waker = WAKER.CON;
    }

    /**
     * Method that will free the lock to talk to the main
     */
    public void releaseMain() {
        waitForMain();

        talkToMain.unlock();
    }

    /**
     * Method that will free the lock to talk to the main, without waiting for the
     * main
     */
    public void unlockMain() {
        requestProcessed = true;
        talkToMain.unlock();
    }

    /**
     * 
     * @return the main's current status, from the enum
     */
    public MAIN_STATUS getStatus() {
        return status;
    }

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        Main main = Main.getInstance();

        main.userInput.start();

        main.joinNetwork();

        // implement here part to connect to a party or start a party
        main.p2pmenu();

        // Here disconnect form network part
        main.exitApp();
    }

    private void endP2PThread(Thread p2p) throws InterruptedException {
        if (p2p.isAlive()) {
            p2p.interrupt();
        }
        p2p.join();
    }

    private void exitApp() throws InterruptedException, IOException {
        System.out.println("Exiting app...");
        P2PConnection conn;
        for (Entry<P2PConnection, Thread> e : this.connectionThreads.entrySet()) {
            conn = e.getKey();
            
            conn.close();

            endP2PThread(e.getValue());
        }

        if (heartbeatThread != null) {
            if (heartbeatThread.isAlive()) {
                heartbeatThread.interrupt();
            }
            heartbeatThread.join();
        }

        stdinWriter.close();
        userInput.join();
    }

    private void joinNetwork() throws UnknownHostException, IOException, InterruptedException {
        justUser = true;

        // configuration of the net:
        System.out.println("Write your IP address");
        String myIP = stdin.nextLine();

        System.out.println("Your IP address is: " + myIP + " Share it with one of the nodes."); // how do we control
                                                                                                // which one?
        System.out.println("Write the IP address of the node next to you: ");
        String ipNeighbour = stdin.nextLine();
        System.out.println("Write the username of the node next to you: ");
        String userNeighbour = stdin.nextLine();

        ServerSocket serverSocket = new ServerSocket(PORT);

        Thread thr = new Thread(() -> {
            try {
                MySocket connectedSocket = new MySocket(serverSocket.accept());
                /* Receive the neighbour's name */
                JSONObject message = connectedSocket.receive();
                String name = message.getString("user");
                connectionThreads.put(new P2PConnection(name, connectedSocket), null);
                serverSocket.close();
            } catch (IOException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        });
        thr.start();
        System.out.println("Server socket created, press ENTER to move to the next step");
        stdin.nextLine();

        P2PConnection nbConnection = new P2PConnection(userNeighbour, ipNeighbour, PORT);

        connectionThreads.put(nbConnection, null);

        JSONObject message = new JSONObject();

        /* Send the node's name to the neighbour */
        System.out.println("Write the user name to send to your neighbour: ");
        userNeighbour = stdin.nextLine();

        message.put("user", userNeighbour);

        nbConnection.send(message);
        System.out.println("Name sent to your neighbour");
        thr.join();
        System.out.println("Joined network successfully");
        justUser = false;


    }

    private void startP2PConnections() {
        connectionThreads.replaceAll((conn, thr) -> {
            if (thr == null)
                thr = new Thread(conn);

            if (!thr.isAlive() && !conn.isClosed())
                thr.start();

            return thr;
        });
    }

    private void p2pmenu() throws UnknownHostException, IOException, InterruptedException {
        startP2PConnections();

        status = MAIN_STATUS.P2P;
        while (true) {
            // shows options to start or join party
            System.out.println("You are not currently in a party");
            System.out.println("Type 'party' to start a new party or wait for an invitation to join an existing party");

            String input = queue.take();

            switch (waker) {
                case WAKER.CON:
                    P2PConnection conn = partyRequests.getWaitingConnection();
                    System.out.println(input);

                    boolean yes = receiveYN();
                    partyRequests.setAnswer(yes);

                    if (yes) {
                        status = MAIN_STATUS.JOIN;
                        joinParty(conn);
                        timeout.join();
                    } else {
                        requestProcessed = true;
                    }
                    break;

                case WAKER.INPUT:
                    switch (input) {
                        case "exit":
                            status = MAIN_STATUS.EXIT;
                            requestProcessed = true;
                            return;

                        case "party":
                            status = MAIN_STATUS.HOST;
                            startParty();
                            timeout.join();
                            break;
                        default:
                            requestProcessed = true;
                            System.out.println("Invalid command \"" + input + "\"");
                            break;
                    }
                    break;

                default:
                    break;
            }

            status = MAIN_STATUS.P2P;
        }
    }

    private void joinParty(P2PConnection hostConnection)
            throws UnknownHostException, IOException, InterruptedException {
        // TODO Do all necessary things to join a party
        /*
         * -Close useless connections
         * -Create a member connection and execute it
         * -Create a music player and a music player thread and execute
         * -Create heartbeat thread
         */

        requestProcessed = true;

        /* Set thread timeout to avoid waiting forever */

        timeout = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT * 1000);
            } catch (InterruptedException e) {
                return;
            }
            talkToMain.lock();
            if (status == MAIN_STATUS.JOIN) {
                requestProcessed = false;

                waker = WAKER.TIMEOUT;

                try {
                    queue.put("Timeout");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                waitForMain();

            }
            talkToMain.unlock();

        });
        timeout.start();

        String partyTime = queue.take();
        if (waker == WAKER.TIMEOUT) {
            requestProcessed = true;
            System.out.println("The host didn't accept you, going back to the menu...");
            return;
        }

        status = MAIN_STATUS.PARTY;
        requestProcessed = true;

        Thread t;

        for (Connection c : this.connectionThreads.keySet()) {

            if (!c.equals(hostConnection)) {
                t = connectionThreads.get(c);
                t.interrupt();
                t.join();

            }
        }

        MemberConnection partyConnection = new MemberConnection(hostConnection);
        sendAction = (a) -> {
            try {
                partyConnection.sendActionRequest(a);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                return;
            }
        };

        partyConnectionThread = new Thread(partyConnection);
        partyConnectionThread.start();

        heartbeat = new MemberHeartbeat();
        heartbeatThread = new Thread(heartbeat);
        heartbeatThread.start();

        playingPartyMenu(Long.valueOf(partyTime));

        if (partyConnectionThread.isAlive()) partyConnectionThread.interrupt();
        partyConnectionThread.join();
    }

    /**
     * This method will do all the necessary preparations for a user to create a
     * playing party
     * it should be called if the user selects "party" in the p2pmenu
     * 
     * @throws InterruptedException
     * @throws IOException
     * @throws UnknownHostException
     */
    private void startParty() throws InterruptedException, UnknownHostException, IOException {
        requestProcessed = true;
        justUser = true;
        System.out.println("Starting party...");

        System.out.println("First, you must select the songs you would like to play in the playing party");
        List<String> partySongs = askUserSongs();
        justUser = false;

        /* Send the request to the other nodes */

        JSONObject request = new JSONObject();

        request.put("type", MessageType.PARTY_REQUEST.toString());
        int num_songs = partySongs.size();
        request.put("num_songs", num_songs);
        for (int i = 0; i < num_songs; i++) {
            request.put("song_" + i, partySongs.get(i));
        }

        P2PConnection.restartTime();
        HostConnection.clearMembers();

        System.out.println("Sending the request...");

        try {
            for (P2PConnection con : connectionThreads.keySet()) {
                con.sendPartyRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        num_party_nodes = 0;

        /* Set thread timeout to avoid waiting forever */
        timeout = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT * 1000);
            } catch (InterruptedException e) {
                return;
            }
            talkToMain.lock();
            if (num_party_nodes == 0) {
                requestProcessed = false;

                waker = WAKER.TIMEOUT;

                try {
                    queue.put("Timeout");
                } catch (InterruptedException e) {
                    talkToMain.unlock();
                    return;
                }

                waitForMain();
            }
            talkToMain.unlock();
        });
        timeout.start();

        String input;
        boolean no;

        boolean exit = false;

        while (!exit && num_party_nodes < max_party_nodes) {
            System.out.println(
                    "Waiting for responses, write \"enough\" if you want to move on to creating the party or write \"exit\" to move to the previous menu");
            input = queue.take();

            switch (waker) {
                case CON:
                    P2PConnection conn = partyAnswers.getWaitingConnection();
                    System.out.println(input);
                    no = !receiveYN();
                    requestProcessed = true;

                    partyAnswers.setAnswer(!no);

                    if (no) {
                        if (num_party_nodes > 0)
                            endP2PThread(connectionThreads.get(conn));
                        continue;
                    }

                    num_party_nodes++;
                    connectionThreads.get(conn).join();

                    HostConnection.addMember(conn);
                    break;
                case INPUT:
                    switch (input) {
                        case "exit":
                            status = MAIN_STATUS.P2P;
                            requestProcessed = true;
                            System.out.println("Exiting...");
                            /* If there have been no replies, then nothing has changed */
                            if (num_party_nodes > 0)
                                HostConnection.clearMembers();

                            return;
                        case "enough":
                            status = MAIN_STATUS.PARTY;
                            requestProcessed = true;
                            exit = true;
                            break;
                        default:
                            requestProcessed = true;
                            System.out.println("Unrecognised input");
                            break;
                    }
                    break;
                case TIMEOUT:
                    System.out.println("The other users are not responding, cancel party?");
                    if (receiveYN()) {
                        status = MAIN_STATUS.P2P;
                        requestProcessed = true;
                        return;
                    }
                    status = MAIN_STATUS.PARTY;
                    requestProcessed = true;
                    exit = true;
                default:
                    break;
            }
        }

        System.out.println("Creating the party...");

        /* Close remaining open threads */
        connectionThreads.forEach((c, t) -> {
            try {
                endP2PThread(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        HostConnection.startMembers();

        long start_time = getNearestChange();
        HostConnection.sendStartParty(start_time);

        sendAction = (a) -> {
            HostConnection.sendActionRequest(a);
        };

        heartbeat = new HostHeartbeat();
        heartbeatThread = new Thread(heartbeat);
        heartbeatThread.start();

        musicPlayerTask = new MusicPlayerTask(new MusicPlayer(partySongs));

        playingPartyMenu(start_time);

        HostConnection.joinMembers();
    }

    /**
     * This method will be called when the node is in a playing party, it will print
     * out the options that the user has and it will process the user's input.
     * This methdo will handle the situation where the user is disconnected from the
     * host
     * 
     * @throws IOException
     * @throws UnknownHostException
     * @throws InterruptedException
     */
    private void playingPartyMenu(long start_time) throws UnknownHostException, IOException, InterruptedException {
        String action;
        Boolean exit = false;

        musicPlayerTask.start(Long.valueOf(start_time));

        status = MAIN_STATUS.PARTY;
        while (!exit) {

            System.out.println("You are in a party! You can use either of these commands:"
                    + "\n- play: if you want to play the music"
                    + "\n- pause: if you want to stop the song"
                    + "\n- forward: if you want to skip to the next song"
                    + "\n- backward: if you want to go back to the previous song"
                    + "\n- exit: if you want to disconnect from the playing party"
                    + "\nNote: if your request is not posible to execute (f.e you skip and it is the last song), your request will be ignored");

            action = queue.take();

            switch (waker) {
                case HEART:
                    System.out.println(action);
                    if (!receiveYN()) {
                        // TODO Not sure but I guess this
                        status = MAIN_STATUS.P2P;
                        requestProcessed = true;
                        return;
                    }
                    requestProcessed = true;
                    continue;
                case INPUT:
                    break;
                default:
                    continue;
            }

            if (action.equals("exit")) {
                status = MAIN_STATUS.P2P;
                requestProcessed = true;
                exit = true;
                musicPlayerTask.stop();
                continue;
            }

            Action matchedAction = Action.match(action);
            requestProcessed = true;
            if (matchedAction == null) {
                System.out.print("The action you entered is not one of the available options");
                continue;
            }

            sendAction.accept(matchedAction);
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
    private boolean receiveYN() {
        Boolean yes = null;

        String answer;

        MAIN_STATUS prevStatus = status;

        justUser = true;

        while (yes == null) {
            answer = stdin.nextLine();
            yes = processYN(answer);

            if (yes == null)
                System.out.println("Error, please write \'yes\' or \'no\'");
        }

        status = prevStatus;
        justUser = false;

        return yes;
    }

    /**
     * This method will process the user input and interpret it as yes, no or
     * neither
     * 
     * @param answer The answer the user has given
     * @return true if the user said 'Yes', false if the user said 'No', null if it
     *         was neither
     */
    private Boolean processYN(String answer) {
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

        return null;
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
            String input = stdin.nextLine();
            if (input.equalsIgnoreCase("done")) {
                break;
            } else if (availableSongs.contains(input)) {
                if (!partySongs.contains(input)) {
                    partySongs.add(input.concat(".mp3"));
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

    /**
     * Method that will create a music player with the songs sent by the host
     * 
     * @param song_list
     */
    public void addPartySongs(List<String> song_list) {
        musicPlayerTask = new MusicPlayerTask(new MusicPlayer(song_list));
    }

    /*******************************************************************************************
     * Connection-related
     *******************************************************************************************/

    /**
     * This method will change the variable in main showing that the heartbeat
     * thread has not heard form the host in too long
     */
    public void notHeardFromHost() {
        talkToMain.lock();
        if (status != MAIN_STATUS.PARTY) {
            talkToMain.unlock();
        }
        requestProcessed = false;
        waker = WAKER.HEART;

        try {
            queue.put("Disconnected from the host, continue playing party? (Y/N)");
        } catch (InterruptedException e) {
            talkToMain.unlock();
            return;
        }

        waitForMain();

        talkToMain.unlock();
    }

    /**
     * @return the amount of seconds ahead of the current time an action has to be
     *         for it to have time to reach all of the nodes and for the nodes to
     *         have time to take all of
     *         the actions
     */
    private long getMilisec() {
        return P2PConnection.getMaxTime();
    }

    /**
     * This method will return the timestamp when the nearest change can be
     * implemented
     * 
     * @return the timestamp when the nearest change can be implemented
     */
    public long getNearestChange() {
        return Instant.now().toEpochMilli() + getMilisec();
    }

    /*******************************************************************************************
     * getters/setters
     *******************************************************************************************/

    public MusicPlayerTask getMusicPlayerTask() {
        return musicPlayerTask;
    }

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    /**
     * @return The SharedInfo for playing party responses
     */
    public SharedInfo getResponse() {
        return partyAnswers;
    }

    /**
     * @return The SharedInfo for playing party requests
     */
    public SharedInfo getRequest() {
        return partyRequests;
    }

}
