package main;
import java.util.Scanner;
import utils.MySocket;
import java.util.concurrent.*;

import music_player.MusicPlayer;
import party.heartbeat.Heartbeat;

import java.time.*;
import java.time.temporal.*;
import java.util.Date;

public class Main {
    private static boolean isParty = false;
    private static String partyOrganizer = "";
    private static Main instance = null;
    private MusicPlayer musicPlayer;
    private Heartbeat heartbeat;
    int port;
    
    String song;
    
    ZonedDateTime time;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("If you want to start a party, write P");
        while (true) {
        	String input = scanner.nextLine();
        	if (input.equalsIgnoreCase("Y")) {
                startParty(scanner);
                
            } 
        	else if (isParty) {
                inviteParty(scanner);
            } 
        }
    }

    public static Main getInstance() {
        if (instance == null) instance = new Main();

        return instance;
    }
    
    /**
     * This method will return the amount of seconds ahead of the current time an action 
     * has to be for it to have time to reach all of the nodes and for the nodes to have 
     * time to take all of the actions
     * @return
     */
    private int getSeconds() {
        /** TODO **/
    }

    /**
     * This method will return the timestamp when the nearest change can be implemented
     * 
     * @return the timestamp when the nearest change can be implemented
     */
    public long getNearestChange() {
        return Instant.now().getEpochSecond() + getSeconds();
    }

    private static void startParty(Scanner scanner) {
        
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
        	MySocket socket= new MySocket(partyOrganizer, port);
            long time_dif = Duration.between(now, time).toMillis();
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> {
                createPlayer(song); 
            }, time_dif, TimeUnit.MILLISECONDS);
        }

        }
        else {}

        public MusicPlayer getMusicPlayer() {
            return musicPlayer;
        }

        public Heartbeat getHeartbeat() {
            return heartbeat;
        }
        

    }

    

}