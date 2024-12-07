package utils;

public enum MessageType {
    ACTION_REQUEST("action_request"),
    PARTY_REQUEST("party_request"),
    PARTY_RESPONSE("party_response"),
    EXECUTE_ACTION("action");


    private String type;

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
