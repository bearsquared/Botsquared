package botsquared;

/**
 * Defines the fields of a command.
 * 
 */

public class Command implements Cloneable {
    
    /**
    * Level defines who can delete, or edit a command, and what fields of that command can be edited.
    * 
    * OP allows moderators and the channel owner to delete the command, or edit the access, visibility, global, delay and output fields.
    * OWNER allows only the channel owner to delete the command, or edit the access, visibility, global, delay and output fields.
    * 
    * The channel owner can change the level of a command from OP to OWNER or vice versa but may not change the level of higher level commands.
    * 
    * NATIVE allows no one to delete the command, and allows only the owner to edit the access, visibility, global, and delay fields.
    * 
    * Commands with a level of OP, OWNER, or NATIVE may be disabled.
    * 
    * FUNCTIONAL allows no one to delete the command and only edit the Access field.
    * This Level indicates that a command may accept parameters and/or perform a function.
    * A FUCNTIONAL command that is called while the bot is in quiet mode or disabled will still perform any function associated with it.
    * Although it will perform the function it will not send any messages in chat with feedback.
    * 
    * COMPLEX allows no one to delete the command or edit any of the fields. These commands may be disabled.
    * This Level is used to indicate that the command has a complex output that is deterministic.
    * 
    * The Level OP is assigned to a command without one explicitly assigned. You can only assign a command a Level of OP or OWNER.
    */
    public enum Level {
        MOD (100, true),
        OWNER (200, true), 
        NATIVE (400, false), 
        FUNCTIONAL (500, false), 
        COMPLEX (600, false);
        
        private final int weight;
        private final boolean settable;
        
        Level(int weight, boolean settable) {
            this.weight = weight;
            this.settable = settable;
        }
        
        public int getWeight() {
            return weight;
        }
        
        public boolean getSettable() {
            return settable;
        }
        
        public static String toList() {
            String s = "";
            for (Level l : Level.values()) {
                if (l.getSettable()) {
                    s += " | " + l.name();
                }
            }
            s = s.replaceFirst(" \\u007c ", "[ ") + " ]";
            return s;
        }
        
        public static boolean contains(String s) {
            for (Level l : Level.values()) {
                if (l.name().equals(s)) {
                    return true;
                }
            }
            return false;
        }
        
        public static boolean getUnknownSettable(String name) {
            if (contains(name)) {
                return Level.valueOf(name).settable;
            }
            return false;
        }
    }
    
    /**
    * Access defines who can call a command.
    * 
    * ALL can be called by anyone.
    * OP can be called by only moderators.
    * OWNER can be called by only the channel owner.
    * BEARSQUARED can be called only by Bearsquared.
    * 
    * The Access ALL is assigned to a command without one explicitly assigned.
    */
    public enum Access {
        PUBLIC (0, true),
        SUB (50, true),
        MOD (100, true), 
        OWNER (200, true), 
        BEARSQUARED (300, false);
        
        private final int weight;
        private final boolean settable;
        
        Access(int weight, boolean settable) {
            this.weight = weight;
            this.settable = settable;
        }
        
        public int getWeight() {
            return weight;
        }
        
        public static boolean contains(String s) {
            for (Access a : Access.values()) {
                if (a.name().equals(s)) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean getSettable() {
            return settable;
        }
        
        public static String toList() {
            String s = "";
            for (Access a : Access.values()) {
                if (a.getSettable()) {
                    s += " | " + a.name();
                }
            }
            s = s.replaceFirst(" \\u007c ", "[ ") + " ]";
            return s;
        }
    }
    
    private String name; //unique command name
    private Level level = Level.MOD; //who can edit or remove this command | *OP, OWNER, NATIVE, FUNCTIONAL, LIST
    private Access access = Access.PUBLIC; //who can call command | *ALL, OP, OWNER
    private boolean global = true; //determines if command acknowledges global timeout | *true, false
    private int delay = 30; //coeffecient that determines the interval at which this command may be called | integer 0-3600, *30
    private transient long lastUsed = 0L; //stores system time the command was last used
    private String output; //string that defines what calling the command does
    
    public Command() {
        
    }
    
    public Command(Command c) {
        this.name = c.getName();
        this.level = c.getLevel();
        this.access = c.getAccess();
        this.global = c.getGlobal();
        this.delay = c.getDelay();
        this.output = c.getOutput();
    }
    
    public boolean equals(Command c) {
        return this.name.equals(c.getName()) &&
                this.level == c.getLevel() &&
                this.access == c.getAccess() &&
                this.global == c.getGlobal() &&
                this.delay == c.getDelay() &&
                this.output.equals(c.getOutput());
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    /**
     * Returns the name of a command Command names must be unique.
     * @return the name of the command as a string.
     */
    public String getName() {
        return name;
    }
    
    public Level getLevel() {
        return level;
    }
    
    public Access getAccess() {
        return access;
    }
    
    public boolean getGlobal() {
        return global;
    }
    
    public int getDelay() {
        return delay;
    }
    
    public long getLastUsed() {
        return lastUsed;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setName(String s) {
        this.name = s;
    }
    
    public void setLevel(Level e) throws IllegalArgumentException {
        level = e;
    }
    
    public void setLevel(String e) throws IllegalArgumentException {
        level = Level.valueOf(e.toUpperCase());
    }
    
    public void setAccess(Access e) throws IllegalArgumentException {
        if (e != Access.BEARSQUARED) {
            access = e;
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    
    public void setAccess(String e) throws IllegalArgumentException {
        access = Access.valueOf(e.toUpperCase());
    }
    
    public void setGlobal(boolean b) throws IllegalArgumentException {
        global = b;
    }
    
    public void setGlobal(String b) throws IllegalArgumentException {
        global = Boolean.valueOf(b);
    }
    
    public void setDelay(int i) throws IllegalArgumentException {
        if (i >= 0 && i <= 3600) {
            delay = i;
        }
        else {
           if (i < 0) {
               delay = 0;
           }
           else if (i > 3600) {
               delay = 3600;
           }
        }
    }
    
    public void setDelay(String s) throws IllegalArgumentException {
        try {
            int i = Integer.parseInt(s);
            if (i >= 0 && i <= 3600) {
                delay = i;
            }
            else {
               if (i < 0) {
                   delay = 0;
               }
               else if (i > 3600) {
                   delay = 3600;
               }
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("0 to 3600");
        }
    }
    
    public void setLastUsed(long l) {
        lastUsed = l;
    }
    
    public void setOutput(String s) {
        output = s;
    }
    
    @Override
    public String toString() {
        return name + level + " "
                + access + " " + global + " "
                + delay + " " + lastUsed + " " + output;
    }
    
    public boolean checkAccess(int senderWeight) {
        return access.getWeight() <= senderWeight;
    }
    
    public boolean checkGlobal(long timeout, int delay) {
        return !global || (System.currentTimeMillis() - ( delay * 1000 ) > timeout );
    }
    
    public boolean checkLevel(int senderWeight) {
        return level.getWeight() <= senderWeight;
    }
    
    public boolean checkTimeout() {
        return System.currentTimeMillis() - ( delay * 1000 ) > lastUsed;
    }
    
    public boolean sendable(int senderWeight, long timeout, int delay) {
        return checkAccess(senderWeight) && checkGlobal(timeout, delay) && checkTimeout();
    }
    
}
