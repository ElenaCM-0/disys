package main;

import java.util.Scanner;

import utils.Connection;
import utils.MySocket;
import java.util.concurrent.*;

import org.json.JSONObject;

import music_player.MusicPlayer;
import music_player.MusicPlayerThread;
import music_player.Update;
import party.Action;
import party.heartbeat.Heartbeat;

import java.io.IOException;
import java.time.*;
import java.time.temporal.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {
    private static boolean isParty = false;
    private static String partyOrganizer = "";
    private static Main instance = null;
    private MusicPlayer musicPlayer;
    private Heartbeat heartbeat;
    private List<Connection> listConnections;
    private static List<String> availableSongs;
    private boolean delayedHeartbeat = false;

    int port;

    String song;

    ZonedDateTime time;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // implement here part to connect to a party or start a party

        // if you are not in a party
        if (!isParty) {
            //shows options to start or join party
            System.out.println("You are not currently in a party");
            System.out.println("Type 'party' to start a new party or 'join' to join an existing party");
            String input = scanner.nextLine();
        
            if (input.equals("party")) {
                askUserSongs(scanner);
                startParty(scanner); //TODO by Niklas?
            } 
            else if (input.equals("join")) {
                inviteParty(scanner);
            }
            else {
                System.out.println("Invalid option; please type 'party' or 'join'");
            }
        } 
        else {
            // if you are in a party:
            Connection connection = new Connection();
            System.out.println("You are in a party! You can use either of these commands:"
                    + "- play: if you want to play de music"
                    + "- pause: if you want to stop the song"
                    + "- forward: if you want to skip to the next song"
                    + "- backward: if you want to go back to the previous song"
                    + "Note: if your request is not posible to execute (f.e you skip and it is the last song), your request will be ignored");
            String action = scanner.nextLine();
            Action matchedAction = Action.match(action);
            if (matchedAction != null) {
                connection.sendActionRequest(matchedAction);
            }
            else if (action.equals("Y") || action.equals("N")){

            }
            else{
                System.out.print("The action you entered is not one of the available options");
            }
        }
    }

    public static Main getInstance() {
        if (instance == null)
            instance = new Main();

        return instance;
    }

    /**
     * This method will return the amount of seconds ahead of the current time an
     * action
     * has to be for it to have time to reach all of the nodes and for the nodes to
     * have
     * time to take all of the actions
     * 
     * @return
     */
    private int getSeconds() {
        /** TODO **/
    }

    /**
     * This method will return the timestamp when the nearest change can be
     * implemented
     * 
     * @return the timestamp when the nearest change can be implemented
     */
    public long getNearestChange() {
        return Instant.now().getEpochSecond() + getSeconds();
    }

    private static void startParty(Scanner scanner) {
        //TODO by Niklas?
        isParty = true
        partyOrganizer = "You"
        System.out.println("You have started a party");
        //...
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
    public List<String> askUserSongs(Scanner scanner) {
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
        scanner.close();
    }

    private static void inviteParty(Scanner scanner) {
        System.out.println(partyOrganizer + "has organized a listening party, do you want to participate? (Y/N)");
        String response = scanner.nextLine();
        if (response.equalsIgnoreCase("Y")) {
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            if (time.isBefore(now)) {
                System.out.print("Sorry, the party started without you!");
                break;
            }
            MySocket socket = new MySocket(partyOrganizer, port);
            long time_dif = Duration.between(now, time).toMillis();
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> {
                createPlayer(song);
            }, time_dif, TimeUnit.MILLISECONDS);
        }

    }

    public MusicPlayerThread getMusicPlayerThread() {
        return musicPlayerThread;
    }

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    /**
     * Method that sends the given JSONObject to all the connections that the main has
     * @param message The object to be sent through the connections
     * @throws IOException 
     */
    public void sendToAllConnections(JSONObject message) throws IOException {
        for(Connection con: listConnections) {
            con.send(message);
        }
    }

    /**
     * This method will change the variable in main showing that the heartbeat thread has not heard form the host in too long
     */
    public void notHeardFromHost() {
        delayedHeartbeat = true;

        System.out.println("Disconnected from the host, continue playing party?");
    }

    public void addAction(Update update) {
        // TODO Auto-generated method stub
    }

    
}
