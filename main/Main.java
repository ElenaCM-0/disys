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

/**
 * Class that manages the main flow of the application and takes care of all of
 * the rest of the threads
 */
public class Main {
    /************************
     * MAIN ENUMS DEFINITION
     ************************/

    /**
     * Enumeratio that represents which thread contacts with main
     */
    public enum WAKER {
        CON, // Connection
        TIMEOUT, // Timeout
        INPUT, // User input
        HEART; // Heartbeat
    }

    /**
     * Enumeration that represents the status of the app in a certain moment
     */
    public enum MAIN_STATUS {
        P2P, // Waiting for commands or invitations to parties
        EXIT, // Exiting app
        JOIN, // Joining a party
        HOST, // In the process of starting a party as a host
        PARTY; // In a playing party
    }

    /******************
     * STATIC CONSTANTS
     ******************/

    private static final int PORT = 1234; // Port to which all sockets are binded

    /******************
     * STATIC VARIABLES
     ******************/

    private static Main instance = null; // Unique instance of Main

    /******************
     * OBJECT CONSTANTS
     ******************/
    private final List<String> availableSongs = List.of("song1", "song2", "song3", "song4"); // Available songs in the
                                                                                             // system
    private final int TIMEOUT = 15; // Number of seconds to wait until timeout is considered
    private final int max_party_nodes = 5; // Maximum number of nodes in a playing party
    private final Lock talkToMain = new ReentrantLock(); // Lock that controls that no more than one thread communicates
                                                         // with main thread at once

    /******************
     * OBJECT VARIABLES
     ******************/

    /************ THREADS *****************/
    private Thread userInput; // Listens to user input
    private Thread heartbeatThread; // Takes care of heartbeat related operations
    private Thread partyConnectionThread; // Runs the connection with the host when the node is a regular member of a
                                          // party
    private Thread timeout; // Waits for timeout and warns main if it's over
    private Map<P2PConnection, Thread> connectionThreads = new HashMap<>(); // Each thread of the map runs a connection
                                                                            // with a certain peer of the network
    private Heartbeat heartbeat; // Object to handle heartbeat functions
    private MusicPlayerTask musicPlayerTask; // Object to handle music playing related actions

    /*** COMMUNICATION BETWEEN THREADS *******/
    private SharedInfo partyRequests = new SharedInfo(); // Information that indicates the connection that has made a
                                                         // request to a party and the answer of the user whether to
                                                         // join the party or not
    private SharedInfo partyAnswers = new SharedInfo(); // Information that indicates the connection that has answer an
                                                        // invitation to a party and the answer of the user whether to
                                                        // accept the member or not
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>(); // Queue where mostv of the times the threads put
                                                                       // the messages to communicate with main thread
    private Scanner stdin = null; // Scanner that main uses to read user input when it's not expecting to receive
                                  // messages from other threads
    private PrintWriter stdinWriter; // Writer used by user input thread to communicate with main thread when main is
                                     // not expecting to receive messages from other threads
    private PipedOutputStream pipedStdinOutput; // Output end of the pipe that user input thread and main thread use to
                                                // communicate
    private boolean requestProcessed; // Variable used to let other threads know when main has finisheed processing
                                      // their request
    private boolean justUser; // Indicates the user input thread that main only expect messages from it

    private WAKER waker; // Indicates main who has put the nessage in the message queue
    private MAIN_STATUS status; // Indicates current status of the app

    /****** OTHER VARIABLES ***************/
    private Consumer<Action> sendAction; // Function to send an action to other peer. It will depend on the role of the
                                         // node in a party (host or member)
    private int num_party_nodes; // Number of nodes that have joined a party

    /****************
     * STATIC METHODS
     ****************/

    /**
     * @return instance of main. Creates it if there is no existing one
     */
    public static Main getInstance() {
        if (instance == null)
            try {
                instance = new Main();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        return instance;
    }

    /**
     * Runs the app
     * 
     * @param args input arguments are ignored
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        /* Create main object */
        Main main = Main.getInstance();

        /* Start thread to listen to user */
        main.userInput.start();

        /* Set up network */
        main.joinNetwork();

        /* Start the app itself */
        main.p2pmenu();

        /* Exit app */
        main.exitApp();
    }

    /****************
     * OBJECT METHODS
     ***************/

    /****** CONSTRUCTOR *******/

    /**
     * Creates main and initializes user input thread
     * 
     * @throws IOException
     */
    private Main() throws IOException {
        /* Open pipe and associate writer and scanner */
        PipedInputStream pipedStdin = new PipedInputStream();
        pipedStdinOutput = new PipedOutputStream(pipedStdin);
        stdinWriter = new PrintWriter(pipedStdinOutput, true);
        stdin = new Scanner(pipedStdin);

        /* User input thread */
        userInput = new Thread(() -> {
            Scanner stdin_real = new Scanner(System.in);
            String input;
            MAIN_STATUS status_before;
            Boolean yn;

            /* Read from system.in */
            while ((input = stdin_real.nextLine()) != null) {
                /* If main just expects input from user, send it through the pipe */
                if (justUser) {
                    stdinWriter.println(input);
                    continue;
                }

                /* Else communicate through message queue */
                status_before = status;
                yn = false;
                try {
                    while (!talkToMain.tryLock(100, TimeUnit.MILLISECONDS)) {
                        /*
                         * If we don*t get the lock but now input from user is expected, we will ignore
                         * previous input
                         */
                        if (justUser) {
                            yn = true;
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
                /* Ignore previous input if now main only requires user input */
                if (yn)
                    continue;

                /*
                 * Else, we acquire the lock. Check the status to ensure main still needs the
                 * input
                 */
                if (status_before != status) {
                    /*
                     * There have been changes in the main before the input was processed, so input
                     * is ignored
                     */
                    talkToMain.unlock();
                    continue;
                }

                /*
                 * If status remains the same, send user input to main and wait until it
                 * processes it
                 */
                requestProcessed = false;
                waker = WAKER.INPUT;
                try {
                    queue.put(input);
                } catch (InterruptedException e) {
                    /* Close scanner if thread is interrupted */
                    stdin_real.close();
                    return;
                }

                waitForMain();

                talkToMain.unlock();

                /* Exit thread when exiting app */
                if (status == MAIN_STATUS.EXIT) {
                    break;
                }
            }
            /* Close scanner before finishing */
            stdin_real.close();
        });
    }

    /******** GETTERS ********/

    /**
     * 
     * @return the main's current status, from the enum
     */
    public MAIN_STATUS getStatus() {
        return status;
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
     * @return the timestamp when the nearest change can be implemented
     */
    public long getNearestChange() {
        return Instant.now().toEpochMilli() + getMilisec();
    }

    /**
     * @return MusicPlayerTask object associated with main
     */
    public MusicPlayerTask getMusicPlayerTask() {
        return musicPlayerTask;
    }

    /**
     * @return Heartbeat object associated with main
     */
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

    /***************** THREAD RELATED *************/

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

    /**
     * Method that will be called by the threads when they want to communicate with
     * main, typically to ask a question to the user
     * 
     * @param question string they want to send
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
        /*
         * The method is called by connections, other threads acquire lock and set
         * requestProcessed to false directly
         */
        waker = WAKER.CON;
    }

    /**
     * Method that will free the lock to talk to the main, waiting until the request
     * is processed
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
     * Interrupts and join a P2P thread
     * 
     * @param p2p thread to interrupt
     * @throws InterruptedException
     */
    private void endP2PThread(Thread p2p) throws InterruptedException {
        if (p2p.isAlive()) {
            p2p.interrupt();
        }
        p2p.join();
    }

    /**
     * Method called by connections when joing a party to create a music player with
     * the songs sent by the host
     * 
     * @param song_list list with the names of the songs for the party
     */
    public void addPartySongs(List<String> song_list) {
        musicPlayerTask = new MusicPlayerTask(new MusicPlayer(song_list));
    }

    /******* INTERACTIONS WITH USER ******/

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

    /****** NETWORK SET UP *******/

    /**
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException
     */
    private void joinNetwork() throws UnknownHostException, IOException, InterruptedException {
        justUser = true;

        /* Get neighbour info */
        System.out.println("Write the IP address of the node next to you: ");
        String ipNeighbour = stdin.nextLine();
        System.out.println("Write the username of the node next to you: ");
        String userNeighbour = stdin.nextLine();

        /* Create server socket and thread to listen to the other neighbour */
        ServerSocket serverSocket = new ServerSocket(PORT);
        Thread thr = new Thread(() -> {
            try {
                MySocket connectedSocket = new MySocket(serverSocket.accept());
                /* Receive the neighbour's name */
                JSONObject message = connectedSocket.receive();
                String name = message.getString("user");
                /* Add new connection */
                connectionThreads.put(new P2PConnection(name, connectedSocket), null);
                serverSocket.close();
            } catch (IOException | InterruptedException e) {
                return;
            }

        });
        thr.start();
        /*
         * Other neighbours should have created their socket server too before moving to
         * the next stage
         */
        System.out.println("Server socket created, press ENTER to move to the next step");
        stdin.nextLine();

        /* Add new connection */
        P2PConnection nbConnection = new P2PConnection(userNeighbour, ipNeighbour, PORT);
        connectionThreads.put(nbConnection, null);

        /* Send the node's name to the neighbour */
        JSONObject message = new JSONObject();
        System.out.println("Write the user name to send to your neighbour: ");
        userNeighbour = stdin.nextLine();
        message.put("user", userNeighbour);
        nbConnection.send(message);
        System.out.println("Name sent to your neighbour");
        /**
         * After the other thread finishes, the node will be connected to both
         * neighbours
         */
        thr.join();
        System.out.println("Joined network successfully");
        justUser = false;

    }

    /****************** P2P PHASE **********************/

    /**
     * Start the threads for all P2PCOnnections, creating them if needed
     */
    private void startP2PConnections() {
        connectionThreads.replaceAll((conn, thr) -> {
            if (thr == null)
                thr = new Thread(conn);

            if (!thr.isAlive() && !conn.isClosed())
                thr.start();

            return thr;
        });
    }

    /**
     * Implements menu for P2P phase in which user can decide to host a party or to
     * wait until receiving an invitation
     * 
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException
     */
    private void p2pmenu() throws UnknownHostException, IOException, InterruptedException {
        /* Start connection threads */
        startP2PConnections();

        status = MAIN_STATUS.P2P;
        while (true) {
            /* Show menu */
            System.out.println("You are not currently in a party");
            System.out.println("Type 'party' to start a new party or wait for an invitation to join an existing party");
            /* Receive input etither from user or a connection */
            String input = queue.take();

            switch (waker) {
                case WAKER.CON:
                    /* We have received an invitation */
                    P2PConnection conn = partyRequests.getWaitingConnection();
                    /* Ask user if they want to join */
                    System.out.println(input);
                    boolean yes = receiveYN();
                    /* Inform connection what the answer has been */
                    partyRequests.setAnswer(yes);

                    if (yes) {
                        /* Join the party if the answer is yes */
                        status = MAIN_STATUS.JOIN;
                        joinParty(conn);
                        /* After the party, join timeout thread */
                        timeout.join();
                    } else {
                        requestProcessed = true;
                    }
                    break;

                case WAKER.INPUT:
                    /* User has typed a command */
                    switch (input) {
                        case "exit":
                            /* Proceed to exit the app */
                            status = MAIN_STATUS.EXIT;
                            requestProcessed = true;
                            return;

                        case "party":
                            /* Proceed to host a party */
                            status = MAIN_STATUS.HOST;
                            startParty();
                            /* After the party, join timeout thread */
                            timeout.join();
                            break;
                        default:
                            /* Command not valid */
                            requestProcessed = true;
                            System.out.println("Invalid command \"" + input + "\"");
                            break;
                    }
                    break;

                default:
                    break;
            }
            /* Make sure the status keeps being P2P after each input received */
            status = MAIN_STATUS.P2P;
        }
    }

    /************** JOIN PARTY ********************/

    /**
     * Executes the process of joining a party, returning back if host doesn't
     * accept this node in its party
     * 
     * @param hostConnection connection with the host
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException
     */
    private void joinParty(P2PConnection hostConnection)
            throws UnknownHostException, IOException, InterruptedException {
        requestProcessed = true;

        /* Set thread timeout to avoid waiting forever */
        timeout = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT * 2 * 1000);
            } catch (InterruptedException e) {
                return;
            }
            /* Get lock to inform main timeout has passed */
            talkToMain.lock();
            if (status == MAIN_STATUS.JOIN) {
                /** Inform only if the status hasn't changed */
                requestProcessed = false;

                waker = WAKER.TIMEOUT;

                try {
                    queue.put("Timeout");
                } catch (InterruptedException e) {
                    return;
                }

                waitForMain();

            }
            /* Unlock main when finished */
            talkToMain.unlock();

        });
        timeout.start();

        /* Wait to receive a message */
        System.out.println("Waiting for the host's reply");
        String partyTime = queue.take();
        if (waker == WAKER.TIMEOUT) {
            /* Timeout has passed, return */
            requestProcessed = true;
            System.out.println("The host didn't accept you, going back to the menu...");
            return;
        }

        /* If waker is not TIMEOUT, it means host has answered, so we start the party */
        System.out.println("Party starting...");
        status = MAIN_STATUS.PARTY;
        requestProcessed = true;

        Thread t;

        /* Close all P2P Connections */
        for (Connection c : this.connectionThreads.keySet()) {
            t = connectionThreads.get(c);
            t.interrupt();
            t.join();

        }
        /* Create a member connection to the host */
        MemberConnection partyConnection = new MemberConnection(hostConnection);
        /*
         * Set set action to be a function that sends a message to the host with the
         * action that the user wants to ecevute
         */
        sendAction = (a) -> {
            try {
                partyConnection.sendActionRequest(a);
            } catch (IOException e) {
                return;
            }
        };
        /* Start connection and heartbeat threads */
        partyConnectionThread = new Thread(partyConnection);
        partyConnectionThread.start();
        heartbeat = new MemberHeartbeat();
        heartbeatThread = new Thread(heartbeat);
        heartbeatThread.start();

        /* Go to party menu to start party */
        playingPartyMenu(Long.valueOf(partyTime));

        /* Join connection thread after the party and go back to p2pMenu */
        if (partyConnectionThread.isAlive())
            partyConnectionThread.interrupt();

        System.out.println("Returning to the playing party menu");

        partyConnectionThread.join();
    }

    /************** START PARTY**************** */

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

        /* Display available songs */
        System.out.println("These are the available songs: ");
        for (String song : availableSongs) {
            System.out.println("- " + song);
        }

        System.out.println("\nType the name of the song you want to add to the party.");
        System.out.println("When you're done, type 'done'.\n");

        while (true) {
            System.out.print("Enter a song: ");
            /* Wsit for the user to enter a song */
            String input = stdin.nextLine();
            if (input.equalsIgnoreCase("done")) {
                /* Exit when finished */
                break;
            } else if (availableSongs.contains(input)) {
                /* Add song to the list if it wasn't already there */
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
     * Method that does all the necessary preparations for a user to create a
     * playing party: asking for songs, sending invitations, allow user to accept or
     * rejected members and switch P2PConnections to HostConnections
     * 
     * @throws InterruptedException
     * @throws IOException
     * @throws UnknownHostException
     */
    private void startParty() throws InterruptedException, UnknownHostException, IOException {
        requestProcessed = true;

        /* Ask user for songs */
        justUser = true;
        System.out.println("Starting party...");
        System.out.println("First, you must select the songs you would like to play in the playing party");
        List<String> partySongs = askUserSongs();
        justUser = false;

        /* Create request to send */

        JSONObject request = new JSONObject();

        request.put("type", MessageType.PARTY_REQUEST.toString());
        int num_songs = partySongs.size();
        request.put("num_songs", num_songs);
        for (int i = 0; i < num_songs; i++) {
            request.put("song_" + i, partySongs.get(i));
        }

        /* Reset Host Connection */
        P2PConnection.restartTime();
        HostConnection.clearMembers();

        /* Send the request to the other nodes */
        System.out.println("Sending the request...");

        try {
            for (P2PConnection con : connectionThreads.keySet()) {
                con.sendPartyRequest(request);
            }
        } catch (IOException e) {
        }

        num_party_nodes = 0;

        /* Set thread timeout to avoid waiting forever */
        timeout = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT * 1000);
            } catch (InterruptedException e) {
                return;
            }
            /* Get lock to inform main timeout has passed */
            talkToMain.lock();
            if (num_party_nodes == 0) {
                /* Inform only if no members have joined */
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
            /* Unlock main before exiting */
            talkToMain.unlock();
        });
        timeout.start();

        /* Wait for peer answers */
        String input;
        boolean no;
        boolean exit = false;

        while (!exit && num_party_nodes < max_party_nodes) {
            System.out.println(
                    "Waiting for responses, write \"enough\" if you want to move on to creating the party or write \"exit\" to move to the previous menu");
            input = queue.take();

            switch (waker) {
                case CON:
                    /* A peer wants to join the party */
                    P2PConnection conn = partyAnswers.getWaitingConnection();
                    /* Ask the user if they want to accept them or not */
                    System.out.println(input);
                    no = !receiveYN();
                    requestProcessed = true;
                    /* Inform the thread about user's answer */
                    partyAnswers.setAnswer(!no);

                    if (no) {
                        if (num_party_nodes > 0)
                            /*
                             * End execution of the connection thread if the party will start anyway without
                             * that node
                             */
                            endP2PThread(connectionThreads.get(conn));
                        continue;
                    }

                    /*
                     * If node is accepted, wait until the connection thrads finishes normally and
                     * add member to the list
                     */
                    num_party_nodes++;
                    connectionThreads.get(conn).join();

                    HostConnection.addMember(conn);
                    break;
                case INPUT:
                    /* User has typed a command */
                    switch (input) {
                        case "exit":
                            /* Go back to P2P network */
                            status = MAIN_STATUS.P2P;
                            requestProcessed = true;
                            System.out.println("Exiting...");
                            /* Delete members who have joined */
                            if (num_party_nodes > 0)
                                HostConnection.clearMembers();

                            return;
                        case "enough":
                            /* Proceed to start the party */
                            status = MAIN_STATUS.PARTY;
                            requestProcessed = true;
                            exit = true;
                            break;
                        default:
                            /* Unvalid command */
                            requestProcessed = true;
                            System.out.println("Unrecognised input");
                            break;
                    }
                    break;
                case TIMEOUT:
                    /**
                     * Nobody answers after timeout. Let the user choose if start the party or not
                     */
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

            }
        });

        /* Start running all host connections */
        HostConnection.startMembers();

        /*
         * Calculate the time needed for a change to be made. 100ms are added to have
         * some margin
         */
        long start_time = getNearestChange() + 100;
        HostConnection.sendStartParty(start_time);

        /*
         * Set setAction function to one that creates and update and sends it to the
         * rest of the members of a party so they synchronize with the changes
         */
        sendAction = (a) -> {
            HostConnection.sendActionRequest(a);
        };

        /* Start heartbeat thread */
        heartbeat = new HostHeartbeat();
        heartbeatThread = new Thread(heartbeat);
        heartbeatThread.start();

        /* Create music player task to handle music playing */
        musicPlayerTask = new MusicPlayerTask(new MusicPlayer(partySongs));

        /* Go to the party menu to start the party */
        playingPartyMenu(start_time);

        /* After a party, join heartbeat and all member connections */
        heartbeatThread.interrupt();
        heartbeatThread.join();

        HostConnection.joinMembers();
    }

    /******************* PARTY TIME ***************************/

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
     * This method will be called when the node is in a playing party, it will print
     * out the options that the user has and it will process the user's input.
     * 
     * @throws IOException
     * @throws UnknownHostException
     * @throws InterruptedException
     */
    private void playingPartyMenu(long start_time) throws UnknownHostException, IOException, InterruptedException {
        String action;
        Boolean exit = false;

        /* Start music playing at the required time */
        musicPlayerTask.start(Long.valueOf(start_time));

        status = MAIN_STATUS.PARTY;
        while (!exit) {

            System.out.println("You are in a party! You can use either of these commands:"
                    + "\n- play: if you want to play the music"
                    + "\n- pause: if you want to stop the song"
                    + "\n- forward: if you want to skip to the next song"
                    + "\n- backward: if you want to go back to the previous song"
                    + "\n- exit: if you want to disconnect from the playing party"
                    + "\nNote: if your request is not possible to execute (f.e you skip and it is the last song), your request will be ignored");
            /* Wait for input */
            action = queue.take();

            switch (waker) {
                /*
                 * It's been too long since the last message from the host (if node is a member)
                 */
                case HEART:
                    /* Ask the user if they want to stay in the party */
                    System.out.println(action);
                    if (!receiveYN()) {
                        exit = true;
                        continue;
                    }
                    requestProcessed = true;
                    continue;
                case INPUT:
                    /* If user input is received handle command */
                    break;
                default:
                    continue;
            }

            /* Exit when the user wants */
            if (action.equals("exit")) {
                exit = true;
                continue;
            }

            /* Else, get the typed action and process it appropiately */
            Action matchedAction = Action.match(action);
            requestProcessed = true;
            if (matchedAction == null) {
                System.out.print("The action you entered is not one of the available options");
                continue;
            }

            sendAction.accept(matchedAction);
        }

        /* When the party is over, stop music and go back to P2P menu */
        status = MAIN_STATUS.P2P;
        requestProcessed = true;
        musicPlayerTask.stop();

    }

    /******************* PARTY TIME **************************/

    /**
     * Free resources, join threads and exit
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    private void exitApp() throws InterruptedException, IOException {
        System.out.println("Exiting app...");
        P2PConnection conn;
        /* Finish and join all P2P connections */
        for (Entry<P2PConnection, Thread> e : this.connectionThreads.entrySet()) {
            try {
                conn = e.getKey();

                conn.close();

                endP2PThread(e.getValue());
            } catch (Exception ex) {
                continue;
            }
        }

        /* Finish and end heartbeat */
        if (heartbeatThread != null) {
            try {
                if (heartbeatThread.isAlive()) {
                    heartbeatThread.interrupt();
                }
                heartbeatThread.join();
            } catch (Exception ex) {
            }
        }

        /** Close scanner and wait for user input thread */
        try {
            stdinWriter.close();
            userInput.join();
        } catch (Exception ex) {
        }

    }

}
