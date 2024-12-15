package party;

import java.util.function.BiFunction;
import javafx.util.Duration;
import music_player.MusicPlayer;
import music_player.PlayerStatus;
import music_player.Status;
import utils.SongInstant;

public enum Action {
    PLAY("play", (mp, pst) -> new PlayerStatus(pst.getInstant(), Status.PLAYING)),
    SKIP("forward", (mp, pst) -> {
        String newSong = mp.getnextSong(pst.getInstant().getSong());
        if (newSong == null) {
            return null;
        }
        return new PlayerStatus(new SongInstant(newSong, Duration.ZERO), pst.getStatus());
    }),
    PAUSE("pause", (c, pst) -> new PlayerStatus(pst.getInstant(), Status.PAUSED)),
    BACK("backward", (mp, pst) -> {
        String newSong = mp.getPreviousSong(pst.getInstant().getSong());
        if (newSong == null) {
            return null;
        }
        return new PlayerStatus(new SongInstant(newSong, Duration.ZERO), pst.getStatus());
    });

    private String command;

    private BiFunction<MusicPlayer, PlayerStatus, PlayerStatus> applyCommand;

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
     * represented by this action. This command doesn change the music player, but
     * simulates what the next status will be
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
