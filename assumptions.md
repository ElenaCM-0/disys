EC: 
**New things**
- I will implement the nodes in the P2P network as a class. This class will have a thread per connection in the P2P network, but the class itself is not a thread. This is, if you are to call one of the methods in the class, it will be the main program that executes them.
    - These threads, for now, are given the ip address and port that they must connect to and they start the connection
- I will assume the user's "friends" are in a txt document in the same folder as the P2P node file. "Friends.txt"
    - I am also going to assume that the node id's are the names of the users and that they are unique. (So, in the messages, the sender id could be something like: "Elena123", which is the same thing that the user would have in it's "Friends.txt" file)
- For now, I am throwing or ingnoring all of the exceptions
- I am going to create an abstract class for threads in sockets to receive and send messages