package music_player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.Action;

public class MusicPlayerThread {

    private MusicPlayer mp;
    private List<Update> updates = new ArrayList<>();

    public MusicPlayerThread(MusicPlayer mp) {
        this.mp = mp;
    }

    public void createUpdate(Action action, long executionTime) {
        
    }

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
