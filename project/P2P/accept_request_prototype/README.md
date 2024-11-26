
## Prototype for the requests

contains the following components:

- **`Host.java`**: The host listens for incoming connections, on a specific port (e.g., 12345), from nodes ("peers") and either accepts or rejects the connection. If accepted, it spawns a new thread to manage the communication with that node.
  
- **`Peer.java`**: 
Name is peer because node.java is already existing
The Peer class attempts to connect to a specified host IP and port. 
Once connected it starts a thread to manage communication with the host.

- **`Connection.java`**: This class manages the communication between the host and a connected peer. It handles receiving and sending messages, as well as processing them based on the message type.

## Running the prototype

- **`Testing the accept_request_prototype`**

1. Open Two Terminal Windows:
You will need two terminal windows to run both the host and the peer processes simultaneously.

2. Start the Host:
In the first terminal navigate to the root directory of project 

Compile the Host (if you haven't already):
``` javac -cp .:lib/json-20210307.jar P2P accept_request_prototype/Host.java ```

Run the Host:
``` java -cp .:lib/json-20210307.jar P2P.accept_request_prototype.Host ```


Expected output in the Host terminal:

- Host is listening to port 12345

This confirms that the Host is waiting for incoming connections on port 12345


3. Start the Peer:
In the second terminal navigate to the root directory of project

Compile the Peer (if you haven't already):
``` javac -cp .:lib/json-20210307.jar P2P/accept_request_prototype/Peer.java ```

Run the Peer:
``` java -cp .:lib/json-20210307.jar P2P.accept_request_prototype.Peer ```

Expected output in the Peer terminal:

- Connected to host at 127.0.0.1:12345
- Received response: accepted

This means that the peer successfully connected to the host at 127.0.0.1 (localhost) on port 12345


4. Host Accepting the Connection Request:
In the Host terminal, after the peer connects you should see additional message confirming that a peer has connected. 

It should look like this:

- Host is listening to the port 12345
- New node connected: 127.0.0.1
- Sent response: accepted

This confirms that the host has accepted the incoming connection from the peer


5. Close the connection:
When done testing, use ``` Ctrl+C ``` in Host terminal to
shut down the host and peer connection