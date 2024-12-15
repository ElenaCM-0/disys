package utils;

/**
 * Enumeration that represents the different types of message sent in the app
 */
public enum MessageType {
    ACTION_REQUEST("action_request"), // To request an actiuon to the host of a party
    PARTY_REQUEST("party_request"), // To invite a node to a party
    PARTY_RESPONSE("party_response"), // To answer to a invitation to a party
    EXECUTE_ACTION("action"), // Used by host to ask the rest of the members of a party to execute an action
    START_PARTY("party"); // Used by the host to inform the rest of the nodes when a prty will start

    private String type;// String that will be sent in the message

    private MessageType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    /**
     * Method that compares the given string with the possible message types
     * 
     * @param type String to compare the enum elements to
     * @return the enum element the string matches or null if it matches no elements
     */
    public static MessageType match(String type) {
        for (MessageType t : MessageType.values()) {
            if ((t.type).equals(type))
                return t;
        }

        return null;
    }
}
