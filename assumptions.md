### EC: 
**New things**
- I am going to create a folder connection for ![alt text](image-5.png) this part of the diagram. I am also going to take out connection.java from the P2P folder, because I don't think it makes sense for it to be there anymore I am also going to create the abstract class party connection inside a folder called party. My idea is to put in that folder all of the playing-party related files and classes
- I have also created the enum Action, but it is not complete.
    - It has a method match that, given a string, it will return the element in the enum it matches. Or null if it's none of them
- I have somewhat changed the connection part of the class diagram, for it to make a bit more sense
- The friends list and everything no longer exists
- I am going to add a method in connection receive(String type) this method is the same as receive, only that it will only return if the message type is the one given. It wil discard all other messages
- If, as the host, I get null in a socket, I will close it and assume that person is no longer in the playing party
- The main stores the time it took for the users to reply to the playing party request
- For UTC time I am using the seconds since EPOCH 

**Questions**
- What do we do with exeptions?
- Why are the methods in main private static?

### CCV:
- I will use a class called MusicPlayer to implement the functionalities related with handling music playing.
- The class is initialized with a list of names of songs (the ones that will be played) and it allows to add new songs.
- I've assumed the songs are stored in some folder called resources/songs. 
- My part for this week is contained in the folder music_player. More information about its structure and how to execute it can be found in the README of that folder. 
