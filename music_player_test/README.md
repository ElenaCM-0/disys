## Folder Structure
- `src`: contains the .java files
    - `exceptions`: implement classes for some exceptions that I have thought about (may be changed depending on how we manage exceptions)
    - `test`: contains the actual implementation of handling music playing
        - `MusicPlayer.java`: contains the class that handles music playing
        - `MusicPlayerApp.java`: this is the programme that I've used to test my code. It contains some graphic interface, but it is NOT intended to be used in the final version; it's just for testing.
-  `lib`: contains all necessary files from the external libraries I used. You have to unzip it ecause some files are too big for GitHub

## Requirements for this part
- Java JDK 21 or newer, You can check this with:
    ```bash
    java --version
    ```
    If you have an older version, you can download a newer one in https://www.oracle.com/java/technologies/downloads/?er=221886#java23
    - Then you will need to install it and configure it for your terminal and/or eclipse (ask me or ChatGPT if you need help for this). 
- Have `openjfx` installed in your system. In Linux, you can easily download it with this command:
    ```bash
    sudo apt install openjfx
    ```
    - It is also necessary to have GStreamer

## How to test it
1. If you have downloaded and installed a new version of Java, make sure it is configured to be used in then terminal. 
2. Unzip lib.zip
3. Create a folder `resources` (in this directory) and inside of it another folder called `songs`.
4. Put inside `songs` two files called `Test_music.mp3`and `Test_music_2.mp3`.
5. Open `music_player`in terminal 
6. Run `run_test.sh`. You should see a box with some autoexplanatory buttons to test each functionality. "Name of the song" and "Time" will be printed on terminal.