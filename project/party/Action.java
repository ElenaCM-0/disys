package party;

import java.util.function.BiFunction;
import javafx.util.Duration;
import music_player.MusicPlayer;
import music_player.PlayerStatus;
import music_player.Status;
import utils.SongInstant;

/**
 * Enumeration that represents actions that can be done durig a party, and
 * contains functions to know the status of the player after applying them
 */
public enum Action {
    PLAY("play", (mp, pst) -> new PlayerStatus(pst.getInstant(), Status.PLAYING)), // Plays the music again if it was
                                                                                   // paused
    SKIP("forward", (mp, pst) -> {
        String newSong = mp.getnextSong(pst.getInstant().getSong());
        if (newSong == null) {
            return null;
        }
        return new PlayerStatus(new SongInstant(newSong, Duration.ZERO), pst.getStatus());
    }), // Sets the player to be in the beginning of the next song, keeping the previous
        // state of the player (PLAYING or PAUSED)
    PAUSE("pause", (c, pst) -> new PlayerStatus(pst.getInstant(), Status.PAUSED)), // Pauses the music
    BACK("backward", (mp, pst) -> {
        String newSong = mp.getPreviousSong(pst.getInstant().getSong());
        if (newSong == null) {
            return null;
        }
        return new PlayerStatus(new SongInstant(newSong, Duration.ZERO), pst.getStatus());
    }) // Sets the player to be in the beginning of the previous song, keeping the
       // previous state of the player (PLAYING or PAUSED)
    ;

    private String command; // Name of the action

    private BiFunction<MusicPlayer, PlayerStatus, PlayerStatus> applyCommand; // Function that returns what the player
                                                                              // status would be after applying an
                                                                              // action taking into account what was the
                                                                              // status before

    private Action(String command, BiFunction<MusicPlayer, PlayerStatus, PlayerStatus> applyCommand) {
        this.command = command;
        this.applyCommand = applyCommand;
    }

    @Override
    public String toString() {
        return command;
    }

    /**
     * Gets the new status of the music player after applying the command
     * represented by this action. This command doesn't change the music player, but
     * simulates what the next status would be
     * 
     * @param mp         music player
     * @param prevStatus status of the music player before applying command
     * @return the new status of the music player after applying the command
     *         represented by this action
     */
    public PlayerStatus apply(MusicPlayer mp, PlayerStatus prevStatus) {
        return this.applyCommand.apply(mp, prevStatus);
    }

    /**
     * Method that compares the given string with the possible actions
     * 
     * @param command String to compare the enum elements to
     * @return the enum element the string matches or null if it matches no elements
     */
    public static Action match(String command) {
        for (Action a : Action.values()) {
            if ((a.command).equals(command))
                return a;
        }

        return null;
    }
}
