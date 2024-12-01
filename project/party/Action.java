package party;

public enum Action {
    PLAY("play"),
    SKIP("forward"),
    PAUSE("pause"),
    BACK("backward");

    String command;
    
    Action(String command) {
        this.command = command;
    }

    /**
     * Method that compares the given string with the possible actions
     * @param command String to compare the enum elements to
     * @return the enum element the string matches or null if it matches no elements
     */
    public static Action match(String command) {
        for (Action a: Action.values()) {
            if ((a.command).equals(command))
                return a;
        }

        return null;
    }
}
