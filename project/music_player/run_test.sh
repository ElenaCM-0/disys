#!/bin/bash

javac -d bin --module-path lib/ --add-modules javafx.controls,javafx.fxml,javafx.media -sourcepath src src/test/MusicPlayerApp.java
java --module-path lib/ --add-modules javafx.controls,javafx.fxml,javafx.media -cp "bin:resources" test.MusicPlayerApp
