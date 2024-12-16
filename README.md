# Synchronized music player
This project is part of the Distributed Systems CSM13001- course

## Folder Structure
- `main`: contains the .java file of the main class

- `music_player`: contains the .java files for all the classes related to music playing
        - `MusicPlayer.java`: contains the class that handles music playing
        - `MusicPlayerTask.java`: contains the class that handles the tasks scheduled for the music player
        - `PlayerStatus.java`: contains the class to get the status and the instant of the song being played
        - `Status.java`: Handles the playback state (PLAYING or PAUSED) of a music player with corresponding actions and state transformations
        - `Update.java`: contains the class that creates the updates for the music player.
        -`exceptions`: contains the exception handling

- `p2p`: contains the .java file which implements the multicast network state
    - `P2PConnection.java`: implements the multicast network state

- `party`: contains the .java files for all the classes related to the party state
    - `heartbeat`
        - `Heartbeat.java`: contains the abstract class which implements the thread which handles heartbeat
        - `HostHeartbeat.java`: contains the class which extends Heartbeat and adds specific functions for the Host heartbeat
        - `MemberHeartbeat.java`: contains the class which extends Heartbeat and adds specific functions for the Host heartbeat
    - `Action.java`: contains the enum class which represents actions (play, pause, skip, back) that can be applied to a music player, simulating the resulting player status without changing the player itself.
    - `HostConnection.java`: contains the class which extends PartyConnection and adds specific functions for the Host Connection
    - `MemberConnection.java`: contains the class which extends PartyConnection and adds specific functions for the Member Connection
    - `PartyConnection.java`: contains the abstract class that extends Connection, providing constructors for establishing a party connection

- `utils`
    - `Connection.java`: contains the abstract class representing a network connection, handling socket communication with peers. It supports sending messages, closing the connection, and checking if it's closed. 
    - `MessageType.java`: contains the enum class which represents different message types used in the communication protocol.
    - `MySocket.java`: contains the custom socket wrapper that simplifies communication via a socket connection.
    - `SharedInfo.java`: contains the class which manages shared data between threads, ensuring thread safety with a lock.
    - `SongInstant.java`: contains the class which represents a specific moment in a song, consisting of the song's name and the corresponding playback time.

-  `lib`: contains all necessary files from the external libraries we've used. You have to unzip mp_lib.zip because some files are too big for GitHub

## Requirements

- Java JDK 21 or newer, You can check this with:
    ```bash
    java --version
    ```
    If you have an older version, you can download a newer one in https://www.oracle.com/java/technologies/downloads/?er=221886#java23
    - Then you will need to install it and configure it for your terminal and/or eclipse. 
- Have `openjfx` installed in your system. In Linux, you can easily download it with this command:
    ```bash
    sudo apt install openjfx
    ```
    Note: The installation instructions for openjfx are specific to Linux. If you are using macOS or Windows, please refer to the official OpenJFX documentation for installation instructions for those platforms.
- It is also necessary to have GStreamer
    Note: GStreamer installation instructions are also for Linux. If you are using another operating system, check the relevant installation guides for macOS or Windows.


## How to test it
1. If you have downloaded and installed a new version of Java, make sure it is configured to be used in the terminal. 
2. Unzip lib.zip
3. Create a folder named `songs` in this directory and place the following 4 sound files inside it: `song1.mp3`, `song2.mp3`, `song3.mp3` and `song4.mp3`. If you wish to use other song names, then you must change line 85 of `Main.java`
4. Connect 3 nodes to a local network.
4. Run `Main.java` on all 3 nodes.
5. When asked "Write the IP address of the node next to you:" only write the part of your IP address which is not the network mask.
6. When the message "Server socket created, press ENTER to move to the next step" appears, wait to press ENTER until all nodes have displayed this message.
