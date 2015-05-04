package botsquared;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a list to hold commands and add, edit, and remove methods.
 * 
 */

public class CommandList {
    
    private Map<String,Command> commands = new HashMap<>();
    
    public Map<String,Command> getCommands() {
        return commands;
    }
    
    public void putCommands(CommandList cl) {
        commands.putAll(cl.getCommands());
    }
}
