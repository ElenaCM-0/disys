# To do:
- Ask teacher:
   + level of complication:
        + Can we assume all nodes have access to the same music files and just control that they play those music files at the same time?
            We can assume all nodes have all the files. We don't need to do everythimng, but acknowledge the things
        + If not, then we will have a central node
   + People behind computers making choices: the nodes will not need to decide all that much.
        + Would that be okay?
            People can make the decisions, don't overthink, have simple scenario
   + What exactly do we have to submit for the 15th the topic thing?
   + Node discovery??? If I know a node's ip address, how do I connect to it taking into account NAT?
      + You do not overcome NAT, use virtual machines or use hotspot or the computers in uni
      + NAT traversal if we want to look it up

# Things to think about
- How does a node in the p2p network notice that the connection to other nodes is lost?
- How do the heartbeat messages from the host work, if 30 secs have passed and I do not have a message, is it bc the host has failed or because there is a small delay?
# Notes:

**Achitecture:**

Decentralised peer to peer

**Middleware:**

Sockets
Not websockets because they are for web
We will not use message-based systems because we want real-time communication

**General idea:**

+ Each node has their own music files which need not be on the other nodes
+ If a node wants to play syncronised music, it will share a request with the other nodes
    + For each node that joins, the main node will share the file, after checking with the node that it does not have it
        + Then, the main node will coordinate the music playing

    + There are three phases:
        + First, the node sends the request and all the nodes reply
           + If two nodes send a request, there is an election
        + Second, the node tries to share the file with the ones that don't have it
        + Third, the node coordinates the music player
            + When the leader node is told to take an action, it will send a message to the other nodes: this action will be taken in UTC whatever time, that time being a few seconds ahead of the node's current time, to give the nodes time to receive the message and take the action

    + **Scalability**
        + We use something like gnutella but for request parties (REDESII P2P slide 15)
        + So the nodes are connected to other nodes but not all nodes
        + The idea is to have several small music parties as opposed to one big one
        + If a node wants to play, they will send a request to neighbouring nodes, these nodes will propagate the request
            + All the nodes that accept will share their ip address and port to generate a connection with the host

        + Once the host has received enough accepts, the playing party begins
            + The playing party begins when either 3 accepts are received or a timeout has passed

    + **General ideas for file transfer**
        
        - One party, one song
        - Each party has a limit of songs to send, for example, five songs per party. Before starting the party, the host node will send all of the songs it wants to play.
        - The party starts when 5 songs have been shared when they are shared, the host can move only between these five songs. While these songs are playing, the host is sharing the rest of the songs in the background
        - How about having playlists? Maybe the host node can chose a playlist and use some bittorrent deal for getting playlists????

    + During the party:
        - The host node can skip the current song and chose the songs to play, only within the playing-pool shared
        - Will other nodes be able to make music choices??

### Project description
+ 2-3 pages long 
+ describe the topic, architecture (nodes and their roles), if it is going to have fases
+ how nodes are going to communicate

### Tasks
Each person should decide how their code with communicate with other people's code, for example, define the method definitions and variables that they have.
When doing the code, if you make any assumptions of what other people have done, inform and write them down so that putting things together is as easy as possible.

# First week (to be done before class on the 27th of November)
- Handle music playing. Have methods that other people can call for: playing a song, skipping to a certain number of seconds in a song (for when a node realises they have fallen behind), pause a song, skip a song forward, go back to the previous song. This person does not have to worry about communication with nodes, just handle the music playing. There should also be a method that can be called that will inform the user the current song and seconds.
- In the P2P network, control when a node receives a message, decides if they will show it to the user, and sends it to other nodes.
- Node accepts request (the person who does this can assume the node knows the ip and port to connect to, maybe on a method) and connects to host and host accepts or rejects node and creates thread and channels to communicate with that node.
- User interface and communication. Doing the 'main'

**Together**
- Figure out hotspot connections
- Put things together

# Second week (to be done before class on the 4th of December)
- Create playing party action requests and send them to the host. As host, process the requests and send them to the other nodes.
- A non-host in the playing party receives a request, processes it and does what has to be done with it, this person must handle the: "this will happen at UTC x" thing.

**Together**
- Put things together

# Third week (to be done before the 9th of December)
- Node discovery
- Heartbeat
- Fault tolerance
- Close threads and channels.
