package test;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MusicPlayerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        List<String> songs = new ArrayList<>();
        songs.add("Test_music.mp3");
        songs.add("Test_music_2.mp3");
        MusicPlayer mp = new MusicPlayer(songs);

        // Botón de reproducción
        Button playButton = new Button("Play");
        playButton.setOnAction(e -> mp.play());

        // Botón de pausa
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> mp.pause());

        Button forward = new Button("Next song");
        forward.setOnAction(e -> mp.nextSong());

        Button back = new Button("Previous song");
        back.setOnAction(e -> mp.previousSong());

        Button moreSeconds = new Button("Skip 3 seconds");
        moreSeconds.setOnAction(e -> mp.advance(3));

        Button name = new Button("Name of the song");
        name.setOnAction(e -> System.out.println(mp.getCurrentSong()));

        Button time = new Button("Time");
        time.setOnAction(e -> System.out.println(mp.getCurrentTime()));

        // Diseño
        HBox controls = new HBox(10, playButton, pauseButton, forward, back, moreSeconds, name, time);
        Scene scene = new Scene(controls, 300, 100);

        primaryStage.setTitle("Reproductor de Música");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
