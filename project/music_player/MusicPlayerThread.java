package music_player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.Action;

import utils.SongInstant;

public class MusicPlayerThread {

    private MusicPlayer mp;
    private List<Update> updates = new ArrayList<>();

    public MusicPlayerThread(MusicPlayer mp) {
        this.mp = mp;
    }

    public void createUpdate(Action action, long executionTime) {

    }

    public SongInstant getPosition(long time) {

        if (updates.isEmpty()) {
            return this.mp.getSongInstantFromNow(time);
        }

        Update lastUpdate = this.updates.getLast();
        SongInstant lastInstant = lastUpdate.geSongInstant();
        if (lastUpdate.getStatus() == Status.PAUSED) {
            return lastInstant;
        }

        return this.mp.getSongInstantFrom(lastInstant, lastUpdate.getExecutionTime(), time);

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
