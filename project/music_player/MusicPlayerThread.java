package music_player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import party.Action;
import utils.SongInstant;

public class MusicPlayerThread {

    private MusicPlayer mp;
    private List<Update> updates = new ArrayList<>();

    public MusicPlayerThread(MusicPlayer mp) {
        this.mp = mp;
    }

    public Update createUpdate(Action action, long executionTime) {
        // TODO
    }

    public SongInstant getPosition(long time) {
        return getStatus(time).getInstant();

    }

    /**
     * @param time The moment when this status is to be obtained
     * @return The status that the player will be in a specified time
     */
    public PlayerStatus getStatus(long time) {

        if (updates.isEmpty()) {
            /** Do we delete past updates? */
            /** TODO */
            return new PlayerStatus(this.mp.getSongInstantFromNow(time), Status.PLAYING);
        }

        Update lastUpdate = this.updates.getLast();
        SongInstant lastInstant = lastUpdate.geSongInstant();
        if (lastUpdate.getStatus() == Status.PAUSED) {
            return new PlayerStatus(lastInstant, Status.PAUSED);
        }

        return new PlayerStatus(this.mp.getSongInstantFrom(lastInstant, lastUpdate.getExecutionTime(), time), Status.PLAYING);
    }

    // Example about how to implement doing an action in a certain UTC time. I
    // copied it just to have the idea abou how the process is done. This is not
    // final code at all
    public static void main(String[] args) {
        // Define el tiempo UTC deseado
        Instant targetTime = Instant.parse("2024-11-27T15:00:00Z");

        // Calcula el tiempo restante hasta el objetivo
        Instant now = Instant.now();
        long delayInMillis = targetTime.toEpochMilli() - now.toEpochMilli();

        if (delayInMillis <= 0) {
            System.out.println("El tiempo especificado ya pasó.");
            return;
        }

        // Crea un programador
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Programa la tarea
        scheduler.schedule(() -> {
            System.out.println("¡Ejecutando tarea a las " + Instant.now() + " UTC!");
        }, delayInMillis, TimeUnit.MILLISECONDS);

        System.out.println("Tarea programada para las " + targetTime + " UTC.");
    }

}
