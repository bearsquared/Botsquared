package botsquared;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.javatuples.*;

/**
 * Defines a list to hold commands and add, edit, and remove methods.
 * 
 */

public class CommandList {
    
    private Map<String,Command> commands = new HashMap<>();
    
    private static final String errorFail = "I encountered an error and was unable to <mode> your command.";
    private static final String exists = " The command <name> already exists.";
    private static final String doesntExist = " The command <name> does not exist.";
    private static final String invalidField = " The field(s) <fields> had an invalid value.";
    private static final String noPermission = " You do not have permission to <mode> this command.";
    private static final String noName = " I was unable to find a valid command name. Command names must start with \"!\".";
    private static final String noOutput = " I was unable to find a valid output.";
    private static final String noParameters = " I was unable to find any valid parameters.";
    private static final String success = "Your command has been <mode>ed successfully <errors> errors.";
    
    public Map<String,Command> getCommands() {
        return commands;
    }
    
    public void putCommands(CommandList cl) {
        commands.putAll(cl.getCommands());
    }
    
    public String toList () {
        String list;
        if (!commands.isEmpty()) {
            boolean notFirst = false;
            
            list = "Here is a list of commands: ";
            
            for (Map.Entry<String, Command> entry : commands.entrySet()) {
                if (notFirst) {
                    list += ", ";
                }
                else {
                    notFirst = true;
                }
                
                list += entry.getKey();
            }
            
            list += ".";
        }
        else {
            list = "There are no custom commands.";
        }
        
        return list;
    }

    
    public Pair<Boolean, String> addCommand(String message) {
        boolean errors = false;
        boolean added = false;
        String feedback = "";
        
        Tuple string = Util.splitMessage(message);
        
        if (commands.get(string.getValue(0).toString()) == null) {
            if (string.getSize() == 3) {
                added = true;
                commands.put(string.getValue(0).toString(), generateCommand(
                            string.getValue(0).toString(),
                            string.getValue(1).toString(),
                            string.getValue(2).toString()));
            }
            else if (string.getSize() == 2) {
                added = true;
                commands.put(string.getValue(0).toString(), generateCommand(
                            string.getValue(0).toString(),
                            string.getValue(1).toString()));
            }
            else if (string.getSize() == 1) {
                if (!string.getValue(0).toString().equalsIgnoreCase("ERROR")) {
                    feedback += errorFail + noOutput;
                }
                else {
                    feedback += errorFail + noName;
                }
            }
        }
        else {
            feedback += errorFail + (errorFail + exists).replaceAll("<name>", string.getValue(0).toString());
        }
        
        if (added) {
            if (!errors) {
                feedback = success.replace("<errors>", "without");
            }
            else {
                feedback = success.replace("<errors>", "with") + feedback;
            }
        }
        
        feedback = feedback.replaceAll("<mode>", "add");
        
        Pair<Boolean, String> result = new Pair<>(added, feedback);
        
        return result;
    }
    
    public Pair<Boolean, String> editCommand(String message, int level) {
        boolean errors = false;
        boolean edited = false;
        String feedback = "";
        
        Tuple string = Util.splitMessage(message);
        
        if (commands.get(string.getValue(0).toString()) != null) {
            try {
                Command o = (Command) commands.get(string.getValue(0).toString()).clone();

                if (o.getLevel().getWeight() <= level) {
                    if (string.getSize() == 3) {
                        edited = !generateCommand(
                                    string.getValue(0).toString(),
                                    string.getValue(1).toString(),
                                    string.getValue(2).toString()).equals(o);
                    }
                    else if (string.getSize() == 2) {
                        edited = !generateCommand(
                                    string.getValue(0).toString(),
                                    string.getValue(1).toString()).equals(o);
                    }
                    else if (string.getSize() == 1) {
                        feedback += errorFail + noOutput;
                    }
                    
                    if (!edited) {
                        feedback += errorFail + " No were changes were made.";
                    }
                }
                else {
                    feedback += errorFail + noPermission;
                }
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(CommandList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            feedback += errorFail + (errorFail + doesntExist).replaceAll("<name>", string.getValue(0).toString());
        }
        
        if (edited) {
            if (!errors) {
                feedback = success.replace("<errors>", "without");
            }
            else {
                feedback = success.replace("<errors>", "with") + feedback;
            }
        }
        
        feedback = feedback.replaceAll("<mode>", "edit");
        
        Pair<Boolean, String> result = new Pair<>(edited, feedback);
        
        return result;
    }
    
    public Pair<Boolean, String> removeCommand(String name, int level) {
        boolean errors = false;
        boolean removed = false;
        String feedback = "";
        
        Matcher m = Util.pExactName.matcher(name);
        
        if (m.matches()) {
            if (commands.get(name) != null) {
                commands.remove(name);

                removed = true;

                feedback += "Your command has been removed successfully.";
            }
            else {
                feedback += (errorFail + doesntExist).replaceAll("<name>", name);
            }
        }
        else {
            feedback += errorFail + noName;
        }
        
        feedback = feedback.replaceAll("<mode>", "remove");
        
        Pair<Boolean, String> result = new Pair<>(removed, feedback);
        
        return result;
    }
    
    private Command generateCommand(String name, String output) {
        Command c;
        
        if (commands.get(name) == null) {
            c = new Command();
            c.setName(name);
            c.setOutput(output);
        }
        else {
            c = commands.get(name);
        }
        
        if (!output.isEmpty()) {
            c.setOutput(output);
        }
        
        return c;
    }
    
    private Command generateCommand(String name, String params, String output) {
        Command c;
        
        if (commands.get(name) == null) {
            c = new Command();
            c.setName(name);
            c.setOutput(output);
        }
        else {
            c = commands.get(name);
        }
        
        Matcher m = Util.pLevel.matcher(params);
        if (m.matches()) {
            c.setLevel(m.group(1));
        }
        
        m = Util.pAccess.matcher(params);
        if (m.matches()) {
            c.setAccess(m.group(1));
        }
        
        m = Util.pGlobal.matcher(params);
        if (m.matches()) {
            c.setGlobal(m.group(1));
        }
        
        m = Util.pDelay.matcher(params);
        if (m.matches()) {
            c.setDelay(m.group(1));
        }
        
        if (!output.isEmpty()) {
            c.setOutput(output);
        }
        
        return c;
    }
    
    public String similar(String message) {
        String name = "NOT FOUND";
        int low = 100;
        int result;
        
        for (Map.Entry<String, Command> pair : commands.entrySet()) {
            result = distance(message, pair.getKey());
            //System.out.println("The difference between " + message + " and " + pair.getKey() + " is " + result);
            
            if (result < low) {
                low = result;
                name = pair.getKey();
            }
        }
        
        int threshold = (int) Math.round(Math.sqrt(name.length()));
        
        if (low <= threshold) {
            return name;
        }
        
        return "NOT FOUND";
    }
    
    public int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        
        int[] costs = new int [b.length() + 1];
        
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        
        return costs[b.length()];
    } 
}
