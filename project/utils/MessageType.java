package utils;

public enum MessageType {
    ACTION_REQUEST("action_request"),
    PARTY_REQUEST("party_request"),
    EXECUTE_ACTION("action");

    private String type;

    private MessageType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
