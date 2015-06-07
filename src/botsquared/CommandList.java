package botsquared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    public Pair<Boolean, String> addCommand(String parameters) {
        boolean errors = false;
        boolean added = false;
        String feedback = "";
        
        if (parameters.startsWith("!")) {
            if (parameters.contains(" ")) {
                String name = parameters.substring(0, parameters.indexOf(" "));
                parameters = parameters.replace(name, "").trim();
                
                if (commands.get(name) == null) {
                    Pair<String, ArrayList<String>> p1 = splitParams(parameters);
                    
                    parameters = p1.getKey();
                    ArrayList<String> fieldParameters = p1.getValue();
                    
                    if (!parameters.isEmpty()) {
                        added = true;
                        
                        Pair<String, Command> p2 = generateCommand(name, fieldParameters, parameters);
                        
                        if (!p2.getKey().isEmpty()) {
                            errors = true;
                            feedback = p2.getKey();
                        }
                        
                        commands.put(name, p2.getValue());
                        
                    }
                    else {
                       feedback += errorFail + noOutput; 
                    }
                }
                else {
                    feedback += errorFail + exists;
                    feedback = feedback.replaceAll("<name>", name);
                }
            }
            else {
                feedback += errorFail + noOutput;
            }
        }
        else {
            feedback += errorFail + noName;
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
    
    public Pair<Boolean, String> editCommand(String parameters, int level) {
        boolean errors = false;
        boolean edited = false;
        String feedback = "";
        
        if (parameters.startsWith("!")) {
            if (parameters.contains(" ")) {
                String name = parameters.substring(0, parameters.indexOf(" "));
                parameters = parameters.replace(name, "").trim();
                
                if (commands.get(name) != null) {
                    try {
                        Command o = (Command) commands.get(name).clone();
                        
                        if (o.getLevel().getWeight() <= level) {
                            Pair<String, ArrayList<String>> p1 = splitParams(parameters);
                            
                            parameters = p1.getKey();
                            ArrayList<String> fieldParameters = p1.getValue();
                            
                            Pair<String, Command> p2 = generateCommand(name, fieldParameters, parameters);
                            
                            if (!p2.getKey().isEmpty()) {
                                errors = true;
                                feedback = p2.getKey();
                            }
                            
                            if (!p2.getValue().equals(o)) {
                                edited = true;
                            }
                            else {
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
                    feedback += (errorFail + doesntExist).replaceAll("<name>", name);
                }
            }
            else {
                feedback += errorFail + noParameters;
            }
        }
        else {
            feedback += errorFail + noName;
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
        
        if (name.startsWith("!")) {
            if (!name.contains(" ")) {
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
        }
        else {
            feedback += errorFail + noName;
        }
        
        feedback = feedback.replaceAll("<mode>", "remove");
        
        Pair<Boolean, String> result = new Pair<>(removed, feedback);
        
        return result;
    }
    
    private Pair<String, ArrayList<String>> splitParams(String parameters) {
        ArrayList<String> fieldParameters = new ArrayList<>();

        Pattern p = Pattern.compile("^\\u003c(.*?)\\u003e"); //Pattern for parameter:value pairs "<param:value>"
        Matcher m = p.matcher(parameters);

        while (m.find()) {//Finds all parameter value pairs and adds to to an array
            fieldParameters.add(m.group(0));
            parameters = parameters.replaceFirst(m.group(0), "").trim();
            m = p.matcher(parameters);
        }
        
        Pair<String, ArrayList<String>> pair = new Pair<>(parameters, fieldParameters);
        
        return pair;
    }
    
    private Pair<String, Command> generateCommand(String name, ArrayList<String> fieldParameters, String output) {
        
        String feedback = "";
        Command c;
        ArrayList<String> fields = new ArrayList<>();
        
        if (commands.get(name) == null) {
            c = new Command();
            c.setName(name);
            c.setOutput(output);
        }
        else {
            c = commands.get(name);
        }
        
        if (!fieldParameters.isEmpty() && fieldParameters.size() > 0) { //Check if there were parameter:value pairs
            
            //Pattern for valid parameter and some field
            Pattern p = Pattern.compile("\\u003c(level|access|visibility|global|delay)\\u003a\\w+\\u003e",
                            Pattern.CASE_INSENSITIVE |
                            Pattern.UNICODE_CASE );

            for (String s : fieldParameters) { //Iterate through each parameter:value pair
                Matcher m = p.matcher(s);
                if (m.matches()) { //Checks if the parameter is valid and has a value

                    //Separate the parameter:value pair into field and value
                    String[] paramPair = s.replaceFirst("<", "").replaceFirst(">", "").split(":");
                    String field = paramPair[0];
                    String value = paramPair[1];

                    //Find the field and set the value to it
                    if (field.equalsIgnoreCase("level")) {
                        try {
                            c.setLevel(value);
                        }
                        catch (IllegalArgumentException e) {
                            fields.add(field);
                        }
                    }
                    else if (field.equalsIgnoreCase("access")) {
                        try {
                            c.setAccess(value);
                        }
                        catch (IllegalArgumentException e) {
                            fields.add(field);
                        }
                    }
                    else if (field.equalsIgnoreCase("global")) {
                        try {
                            c.setGlobal(value);
                        }
                        catch (IllegalArgumentException e) {
                            fields.add(field);
                        }
                    }
                    else if (field.equalsIgnoreCase("delay")) {
                        try {
                            c.setDelay(value);
                        }
                        catch (Exception e) {
                            fields.add(field);
                        }
                    }
                    
                    
                }
            }
        }
        
        if (!output.isEmpty()) {
            c.setOutput(output);
        }
            
        if (!fields.isEmpty()) {
            feedback = invalidField.replace("<fields>", fields.toString().replace("[","").replace("]",""));
        }
        
        Pair<String, Command> pair = new Pair<>(feedback, c);
        
        return pair;
    }
}
