package botsquared;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jibble.pircbot.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Creates, reads, updates, removes and handles chat commands.
 * 
 */
public class Botsquared extends PircBot {
    
    private Map<String, Channel> channels = new HashMap<>();
    
    CommandList natives = new CommandList();
    
    ArrayList<String> whitelist = new ArrayList<>(); //This list stores the users that are permitted to post one link in chat.
    
        /**
         * Sets the nick of the bot.
         */
	public Botsquared() {
		this.setName("Botsquared");
	}
        
        /**
         * Sets the initial value of variables and deserializes commands from config.json and #[channel].json and adds them to the commands list.
         * 
         * @param channel
         * 
         * @throws FileNotFoundException
         * @throws UnsupportedEncodingException
         * @throws CloneNotSupportedException 
         */
        public void initializeBot(String channel) throws FileNotFoundException, UnsupportedEncodingException, CloneNotSupportedException {
            
            sendRawLine("TWITCHCLIENT 3");
            try {
                
                File f = new File(channel + ".json");
                Gson gson = new Gson();
                
                //Read in native commands
                try (BufferedReader br1 = new BufferedReader(new FileReader("config.json"))) {
                    Type listType = new TypeToken<CommandList>() {}.getType();
                    natives = gson.fromJson(br1, CommandList.class);
                }
                
                //Read in channel commands
                if (f.exists() && f.length() != 0) {
                    try (BufferedReader br2 = new BufferedReader(new FileReader(f))) {
                        Type channelType = new TypeToken<Channel>() {}.getType();
                        channels.put(channel, gson.fromJson(br2, Channel.class));
                        channels.get(channel).clearModList();
                        channels.get(channel).clearSuperList();
                    }
                }
                
                else {
                    String json = gson.toJson(new Channel(channel));
                    try {
                        try (FileWriter writer = new FileWriter(channel + ".json")) {
                            writer.write(json);
                        }
                    } catch (IOException e) {

                    }
                }
                
            } catch (IOException e) {
                
            }
        }
    
        /**
         * Calls methods that need to run when the bot joins a channel.
         * This method is called whenever anyone joins the channel but only executes code if the user joining the channel was itself.
         * 
         * @param channel
         * @param sender
         * @param login
         * @param hostname 
         */
    @Override
        public void onJoin(String channel, String sender, String login, String hostname) {
            //Check name of user joining
            if (sender.equalsIgnoreCase(this.getName())) {
                try {
                    initializeBot(channel);
                    //sendMessage(channel, "I have the joined the channel.");
                } catch (FileNotFoundException | UnsupportedEncodingException | CloneNotSupportedException ex) {
                    Logger.getLogger(Botsquared.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }
        
        /**
         * This method handles messages sent in chat.
         * 
         * @param channel
         * @param sender
         * @param login
         * @param hostname
         * @param message 
         */
    @Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
            
            //Subscriber/Turbo/Staff check
            if (sender.equalsIgnoreCase("jtv")) {
                String[] words = message.split(" ");
                if (words[0].equalsIgnoreCase("SPECIALUSER")) {
                    if (words[2].equalsIgnoreCase("subscriber") || words[2].equalsIgnoreCase("turbo") || words[2].equalsIgnoreCase("staff")) {
                        if (!channels.get(channel).getSuperList().contains(words[1])) {
                            channels.get(channel).getSuperList().add(words[1]);
                        }
                        buildJson(channel);
                    }
                }
            }
            
            if (message.startsWith("!")) { // Messages that start with "!" are assumed to be a command.
                CommandList thisChannel = new CommandList();
                thisChannel.putCommands(natives);
                thisChannel.putCommands(channels.get(channel).getList());
                Command c = thisChannel.getCommands().get(message);
                if (c != null) {
                    if (c.sendable(checkWeight(channel, sender), channels.get(channel).getGlobalTimeout(), channels.get(channel).getGlobalDelay())) {
                        if (c.getLevel() != Command.Level.COMPLEX) {
                            if (c.getGlobal()) {
                                channels.get(channel).setGlobalTimeout(System.currentTimeMillis());
                            }

                            c.setLastUsed(System.currentTimeMillis());

                            sendMessage(channel, c.getOutput());
                        }
                        else {
                            if (c.getName().equalsIgnoreCase("!join") && channel.equalsIgnoreCase("#" + getName())) {
                                joinChannel("#" + sender);
                            }
                            else if (c.getName().equalsIgnoreCase("!leave") && c.checkAccess(checkWeight(channel, sender))) {
                                sendMessage(channel, c.getOutput());
                                partChannel(channel);
                            }
                            else if (c.getName().equalsIgnoreCase("!list")) {
                                sendMessage(channel, thisChannel.getCommands().keySet().toString());
                            }
                            else if (c.getName().equalsIgnoreCase("!uptime")) {
                                String html = "http://nightdev.com/hosted/uptime.php?channel=" + channel.replaceFirst("#", "");
                                try {
                                    Document doc = Jsoup.connect(html).get();
                                    String text = doc.body().text();
                                    if (text.equalsIgnoreCase("The channel is not live.")) {
                                        sendMessage(channel, text);
                                    }
                                    else {
                                        sendMessage(channel, channel.replaceFirst("#", "") + c.getOutput() + text + ".");
                                    }
                                } catch (IOException ex) {
                                    Logger.getLogger(Botsquared.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
                else if (message.contains(" ")) {
                    c = natives.getCommands().get(message.substring(0, message.indexOf(" ")));

                    if (c != null && c.checkAccess(checkWeight(channel, sender))) {
                        if (c.getName().equalsIgnoreCase("!command")) {
                            handleCommand(channel, message, sender);
                        }
                        else if (c.getName().equalsIgnoreCase("!join") && channel.equalsIgnoreCase("#" + getName()) && sender.equalsIgnoreCase("bearsquared")) {
                                handleJoin(message);
                        }
                        else if (c.getName().equalsIgnoreCase("!moderate")) {
                            handleModerate(channel, message);
                        }
                        else if (c.getName().equalsIgnoreCase("!permit")) {
                            handlePermit(channel, message);
                        }
                        else if (c.getName().equalsIgnoreCase("!poll")) {
                            handlePoll(channel, message);
                        }
                        else if (c.getName().equalsIgnoreCase("!subnotify")) {
                            handleSubNotify(channel, message);
                        }
                        else if (c.getName().equalsIgnoreCase("!vote")) {
                            handleVote(channel, sender, message);
                        }
                    }
                }
            }
            // If moderate is true then it will also check messages sent by normal users for URLs.
            else if (isLink(message) && !isOp(channel, sender) && channels.get(channel).getModerate() && !channels.get(channel).getSuperList().contains(sender)) {
                if (whitelist.contains(sender)) {
                    whitelist.remove(sender);
                }
                else {
                    sendMessage(channel, "/timeout " + sender + " 1");//Timeout sender for 1 second to delete message
                    sendMessage(channel, "Please ask permissions before posting links " + sender + ".");
                }
            }
            
            // Subscriber notification
            else if (sender.equalsIgnoreCase("twitchnotify")) {
                String[] words = message.split(" ");
                if (words[2].equalsIgnoreCase("subscribed!")) {
                    String notify = channels.get(channel).getSubMessage();
                    notify = notify.replaceAll("<user>", words[0]);
                    sendMessage(channel, notify);
                }
                else if (words[1].equalsIgnoreCase("subscribed")) {
                    sendMessage(channel, "Thanks " + words[0] + " for subscribing for " + words[3] + " months in a row!");
                }
            }
	}
        
    @Override
        public void onUserMode(String target, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
            if (mode.contains("+o")) {// Check if user was modded
                String[] u = mode.split("\\+o");
                u[0] = u[0].trim();
                u[1] = u[1].trim();
                if (!u[1].isEmpty() && !channels.get(u[0]).getModList().contains(u[1])) {
                    channels.get(u[0]).getModList().add(u[1]);
                    buildJson(u[0]);
                }
            }
            
            if (mode.contains("-o")) {// Check if user was unmodded
                String[] u = mode.split("\\-o");
                u[0] = u[0].trim();
                u[1] = u[1].trim();
                if (!u[1].isEmpty() && channels.get(u[0]).getModList().contains(u[1])) {
                    channels.get(u[0]).getModList().remove(u[1]);
                    buildJson(u[0]);
                }
            }
        }
        
        /**
         * Constructs an error string for descriptive feedback.
         * 
         * @param field
         * @param values
         * @return 
         */
        public String buildError(String field, String values) {
            String error = " The field " + Character.toUpperCase(field.charAt(0)) + field.substring(1) + " must have a value of " + values + ".";
            return error;
        }
        
        /**
         * Serializes the channel's command list to JSON.
         * 
         * @param channel 
         */
        public void buildJson(String channel) {
            File f = new File(channel + ".json");
            Gson gson = new Gson();
            String json = gson.toJson(channels.get(channel));
            try {
                try (FileWriter writer = new FileWriter(channel + ".json")) {
                    writer.write(json);
                }
            } catch (IOException e) {

            }
        }
        
        /**
         * Constructs a partial string to provide feedback when a user tries to edit a command's field they do not have permission to.
         * 
         * @param current
         * @param field
         * @return 
         */
        public String buildPermsVio(String current, String field) {
            String s = current;
            if (s.isEmpty()) {
                field = Character.toUpperCase(field.charAt(0)) + field.substring(1);
                s = " You do not have permission to edit the " + field;
            }
            else {
                field = Character.toUpperCase(field.charAt(0)) + field.substring(1);
                s += " " + field;
            }
            return s;
        }
        
        /**
         * Checks if a message contains a URL
         * 
         * @param message
         * @return 
         */
        public boolean isLink(String message) {
        String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        String [] parts = message.split("\\s");
            for( String item : parts ) try {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(item);
                if(matcher.matches()) {
                    return true; 
                }
            } catch (RuntimeException e) {

            }
            return false;
        
        }
        
        /**
         * Check the list of known moderators for the channel and return if the sender is found.
         * 
         * @param channel
         * @param sender
         * @return 
         */
        public boolean isOp(String channel, String sender) { 
            return channels.get(channel).getModList().stream().anyMatch((u) -> (u.equalsIgnoreCase(sender)));
        }
        
        /**
         * Checks if the sender is the owner of the channel.
         * 
         * @param channel
         * @param sender
         * @return 
         */
        public boolean isOwner(String channel, String sender) {
            if (channel.startsWith("#")) {
                String o;
                o = channel.replace("#", "");
                return o.equalsIgnoreCase(sender);
            }
            return false;
        }
        
        public int checkWeight(String channel, String sender) {
            if (sender.equalsIgnoreCase("bearsquared")) {
                return 300;
            }
            else if (isOwner(channel, sender)) {
                return 200;
            }
            else if (isOp(channel, sender)) {
                return 100;
            }
            else {
                return 0;
            }
        }
        
        public void handleJoin(String message) {
            String call = "!join ";
            String parameter = message.replace(call, "").trim();
            if (!parameter.contains(" ")) {
                joinChannel("#" + parameter);
            }
        }
        
        /**
         * Is used to tell the bot to moderate the channel and what to moderate.
         * The bot currently only deletes links.
         * 
         * @param channel
         * @param message
         */
        public void handleModerate(String channel, String message) {
            String call = "!moderate ";
            String parameter = message.replace(call, "").trim();
            if (isOp(channel, this.getName())) {
                if (parameter.equalsIgnoreCase("links") || parameter.equalsIgnoreCase("link")) {
                    if (!channels.get(channel).getModerate()) {
                        channels.get(channel).setModerate(true);
                        sendMessage(channel, "I will now delete links.");
                    }
                    else {
                        channels.get(channel).setModerate(false);
                        sendMessage(channel, "I will no longer delete links.");
                    }
                }
                else {
                    sendMessage(channel, "The command \"!moderate\" only accepts \"links\" as a parameter.");
                }
            }
            else {
                sendMessage(channel, "I have to be a moderator to moderate the chat. If you're seeing this message and I am a moderator, try to enable this mode again in a few minutes.");
            }
        }
        
        /**
         * Allows the user to post a link if moderate is enabled.
         * 
         * @param channel
         * @param message
         */
        public void handlePermit(String channel, String message) {
            if (channels.get(channel).getModerate()) { //Check if moderate is true
                String call = "!permit ";
                String parameter = message.replace(call, "").trim();

                if(!parameter.isEmpty() && parameter.length() > 0) {
                    if (!whitelist.contains(parameter)) {
                        whitelist.add(parameter);
                        sendMessage(channel, "You have permitted " + parameter + " to post one link.");
                    }
                    else {
                        sendMessage(channel, parameter + " already has permission to post a link.");
                    }
                }
                else {
                    sendMessage(channel, "I couldn't find the name of a user. Use the pattern \"!permit [name of user]\" to allow them to post one link.");
                }
            }
        }
        
        public void handlePoll(String channel, String message) {
            String call = "!poll ";
            String parameter = message.replace(call, "").trim();
            
            if (parameter.startsWith("open")) {
                parameter = parameter.replace("open", "").trim();
                if (parameter.contains(" ")) {
                    String[] options = parameter.split("\\|");
                    ArrayList<PollOption> poll = new ArrayList<>();
                    for (String option : options) {
                        poll.add(new PollOption(option.trim()));
                    }
                    sendMessage(channel, channels.get(channel).getPoll().open(poll));
                    buildJson(channel);
                }
                else {
                    sendMessage(channel, "Sorry, I didn't find any options for your poll.");
                }
            }
            else if (parameter.startsWith("close")) {
                sendMessage(channel, channels.get(channel).getPoll().close());
                buildJson(channel);
            }
            else if (parameter.startsWith("reset")) {
                sendMessage(channel, channels.get(channel).getPoll().reset());
                buildJson(channel);
            }
            else if (parameter.startsWith("results")) {
                sendMessage(channel, channels.get(channel).getPoll().results());
                buildJson(channel);
            }
        }
        
        public void handleSubNotify(String channel, String message) {
            String call = "!subnotify ";
            String parameter = message.replace(call, "").trim();
            
            if(!parameter.isEmpty() && parameter.length() > 0) {
                channels.get(channel).setSubMessage(parameter);
                buildJson(channel);
                sendMessage(channel, "You have set the subscriber notification message successfully.");
            }
        }
        
        public void handleVote(String channel, String sender, String message) throws IllegalArgumentException {
            String call = "!vote ";
            String parameter = message.replace(call, "").trim();
            
            try {
                if(!parameter.isEmpty() && parameter.length() > 0) {
                    channels.get(channel).getPoll().vote(sender, Integer.parseInt(parameter));
                }
            }
            catch (NumberFormatException e) {
                
            }
        }
        
        /**
         * Parses a !command string and performs the requisite action.
         * 
         * @param channel
         * @param message
         * @param sender 
         */
        public void handleCommand(String channel, String message, String sender) {
            String call = "!command";
            String parameters = message.replace(call, "").trim();
            
            CommandList thisChannel = new CommandList();
            thisChannel.putCommands(natives);
            thisChannel.putCommands(channels.get(channel).getList());
            Command c = thisChannel.getCommands().get(call);

            if (parameters.equalsIgnoreCase("add")) {
                sendMessage(channel, "To add a command use the format \"!command add ![name] [output]\".");
            }
            else if (parameters.startsWith("add ")) {
                parameters = parameters.replace("add ", "").trim();  
                handleCommandAdd(channel, parameters);
            }
            else if (parameters.equalsIgnoreCase("edit")) {
                sendMessage(channel, "To edit a command use the format \"!command edit ![name] [new output]\".");
            }
            else if (parameters.startsWith("edit ")) {
                parameters = parameters.replace("edit ", "").trim(); 
                handleCommandEdit(channel, sender, parameters);
            }
            else if (parameters.equalsIgnoreCase("remove")) {
                sendMessage(channel, "To remove a command use the format \"!command remove ![name]\".");
            }
            else if (parameters.startsWith("remove ")) {
                parameters = parameters.replace("remove ", "").trim(); 
                handleCommandRemove(channel, sender, parameters);
            }
            else {
                sendMessage(channel, "I didn't recognize the parameter after !command.");
            }
        }
        
        /**
         * Adds a command to channel and serializes it to JSON.
         * 
         * @param channel
         * @param parameters
         */
        public void handleCommandAdd(String channel, String parameters) {
            String formatRemind = " Make sure you use the format \"!command add ![name] [output]\".";
            String noName = "It looked like you tried to add a command but I was unable to find a valid command name." + formatRemind;
            String noOutput = "It looked like you tried to add a command but I was unable to find an output." + formatRemind;
            
            CommandList thisChannel = new CommandList();
            thisChannel.putCommands(natives);
            thisChannel.putCommands(channels.get(channel).getList());
            
            if (parameters.contains(" ") && parameters.startsWith("!")) { //Check for Name and parameters (Block 1)
                String name = parameters.substring(0, parameters.indexOf(" "));
                parameters = parameters.replace(name, "").trim();
                
                Command c = thisChannel.getCommands().get(name);
                
                if (c == null) { //Check if command exists (Block 2)
                    ArrayList<String> fieldParameters = new ArrayList<>();

                    Pattern p = Pattern.compile("^\\u003c(.*?)\\u003e"); //Pattern for parameter:value pairs "<param:value>"
                    Matcher m = p.matcher(parameters);

                    while (m.find()) { //Finds all parameter value pairs and adds to to an array
                        fieldParameters.add(m.group(0));
                        parameters = parameters.replaceFirst(m.group(0), "").trim();
                        m = p.matcher(parameters);
                    }
                    if (!parameters.isEmpty()) {//Check for an output Block 3
                        String output = parameters;

                        Command toAdd = new Command();
                        toAdd.setName(name);
                        toAdd.setOutput(output);

                        String badParams = "";
                        String paramErrors = "";

                        if (!fieldParameters.isEmpty() && fieldParameters.size() > 0) { //Check if there were parameter:value pairs
                            
                            //Pattern for valid parameter and some field
                            p = Pattern.compile("\\u003c(level|access|visibility|global|delay)\\u003a\\w+\\u003e",
                                            Pattern.CASE_INSENSITIVE |
                                            Pattern.UNICODE_CASE );

                            for (String s : fieldParameters) { //Iterate through each parameter:value pair
                                m = p.matcher(s);
                                if (m.matches()) { //Checks if the parameter is valid and has a value
                                    
                                    //Separate the parameter:value pair into field and value
                                    String[] paramPair = s.replaceFirst("<", "").replaceFirst(">", "").split(":");
                                    String field = paramPair[0];
                                    String value = paramPair[1];
                                    
                                    //Find the field and set the value to it
                                    if (field.equalsIgnoreCase("level")) {
                                        try {
                                            toAdd.setLevel(value);
                                        }
                                        catch (IllegalArgumentException e) {
                                            paramErrors += buildError(field, Command.Level.toList());
                                        }
                                    }
                                    else if (field.equalsIgnoreCase("access")) {
                                        try {
                                            toAdd.setAccess(value);
                                        }
                                        catch (IllegalArgumentException e) {
                                            paramErrors += buildError(field, Command.Access.toList());
                                        }
                                    }
                                    else if (field.equalsIgnoreCase("visibility")) {
                                        try {
                                            toAdd.setVisibility(value);
                                        }
                                        catch (IllegalArgumentException e) {
                                            paramErrors += buildError(field, Command.Visibility.toList());
                                        }
                                    }
                                    else if (field.equalsIgnoreCase("global")) {
                                        try {
                                            toAdd.setGlobal(value);
                                        }
                                        catch (IllegalArgumentException e) {
                                            paramErrors += buildError(field,"true or false");
                                        }
                                    }
                                    else if (field.equalsIgnoreCase("delay")) {
                                        try {
                                            toAdd.setDelay(value);
                                        }
                                        catch (Exception e) {
                                            paramErrors += buildError(field, e.getMessage());
                                        }
                                    }
                                }
                                else {
                                    if (badParams.isEmpty()) {
                                        badParams = " The parameter(s) " + s;
                                    }
                                    else {
                                        badParams += " " + s;
                                    }
                                }
                            }

                            if (!badParams.isEmpty()) {
                                badParams += " had an incorrect field name or was formatted incorrectly. Parameters must match the pattern \"<[field]:[value]>\".";
                            }

                            paramErrors += badParams;
                        }
                        
                        //Add the command to the command lists and serialize them.
                        
                        channels.get(channel).getList().getCommands().put(name, toAdd);

                        buildJson(channel);

                        sendMessage(channel, "Your command " + name + " has been added successfully." + paramErrors);
                    }//End Block 3 If
                    
                    else {//Block 3 Else
                        if (parameters.startsWith("!")) {
                            sendMessage(channel, noOutput);
                        }
                        else {
                            sendMessage(channel, noName);
                        }
                    }//End Block 3 Else
                }//End Block 2 If
                
                else {//Start Block 2 Else
                    sendMessage(channel, "The command " + name + " already exists. If you were trying to edit this command use !command edit instead.");
                }//End Block 2 Else
                
            }//End Block 1 If
            
            else {//Block 1 Else
                sendMessage(channel, noOutput);
            }//End Block 1 Else
        }
        
        /**
         * Edits a command's parameters and serializes it to JSON.
         * 
         * @param channel
         * @param sender
         * @param parameters
         */
        public void handleCommandEdit(String channel, String sender, String parameters) {
            String formatRemind = " Make sure you use the format \"!command edit ![name] [output]\".";
            String noName = "It looked like you tried to edit a command but I was unable to find a valid command name." + formatRemind;
            String noOutput = "It looked like you tried to edit a command but I was unable to find an output." + formatRemind;
            
            CommandList thisChannel = new CommandList();
            thisChannel.putCommands(natives);
            thisChannel.putCommands(channels.get(channel).getList());
            
            if (parameters.contains(" ") && parameters.startsWith("!")) {//Check for Name and parameters (Block 1)
                
                String name = parameters.substring(0, parameters.indexOf(" "));
                
                Command c = thisChannel.getCommands().get(name);
                
                if (c != null) {//Check if command exists (Block 2)
                    
                    if (c.checkLevel(checkWeight(channel, sender))) {//Check if command is editable (Block 3)
                        
                        parameters = parameters.replace(name, "").trim();
                        ArrayList<String> fieldParameters = new ArrayList<>();

                        Pattern p = Pattern.compile("^\\u003c(.*?)\\u003e"); //Pattern for parameter:value pairs "<param:value>"
                        Matcher m = p.matcher(parameters);

                        while (m.find()) {//Finds all parameter value pairs and adds to to an array
                            fieldParameters.add(m.group(0));
                            parameters = parameters.replaceFirst(m.group(0), "").trim();
                            m = p.matcher(parameters);
                        }

                        String output = "";
                        if (!parameters.isEmpty()) {
                            output = parameters;
                        }

                        String badField = "";
                        String badParams = "";
                        String paramErrors = "";
                        String permissionViolations = "";
                        String delayWarning = "";

                        Command toEdit = new Command();
                        toEdit.setName(name);
                        toEdit.setLevel(c.getLevel());
                        toEdit.setAccess(c.getAccess());
                        toEdit.setVisibility(c.getVisibility());
                        toEdit.setGlobal(c.getGlobal());
                        toEdit.setDelay(c.getDelay());
                        toEdit.setOutput(c.getOutput());

                        if (!output.isEmpty()) {
                            if (c.getLevel().getWeight() < 200) {
                                toEdit.setOutput(output);
                            }
                            else {
                                permissionViolations += buildPermsVio(permissionViolations, "Output");
                            }
                        }

                        if (!fieldParameters.isEmpty() && fieldParameters.size() > 0) { //Check if there were parameter:value pairs
                            
                            //Pattern for valid parameter and some field
                            p = Pattern.compile("\\u003c(level|access|visibility|global|delay)\\u003a\\w+\\u003e",
                                            Pattern.CASE_INSENSITIVE |
                                            Pattern.UNICODE_CASE );

                            for (String s : fieldParameters) { //Iterate through each parameter:value pair
                                m = p.matcher(s);
                                if (m.matches()) { //Checks if the parameter is valid and has a value
                                    
                                    //Separate the parameter:value pair into field and value
                                    String[] paramPair = s.replaceFirst("<", "").replaceFirst(">", "").split(":");
                                    String field = paramPair[0];
                                    String value = paramPair[1];
                                    
                                    //Find the field and set the value to it
                                    if (field.equalsIgnoreCase("level")) {
                                        if (c.getLevel().getWeight() < 200) {
                                            try {
                                                toEdit.setLevel(value);
                                            }
                                            catch (IllegalArgumentException e) {
                                                paramErrors += buildError(field, Command.Level.toList());
                                            }
                                        }
                                        else {
                                            permissionViolations += buildPermsVio(permissionViolations, field);
                                        }
                                    }
                                    else if (field.equalsIgnoreCase("access")) {
                                        if (c.getLevel().getWeight() < 400) {
                                            try {
                                                toEdit.setAccess(value);
                                            }
                                            catch (IllegalArgumentException e) {
                                                paramErrors += buildError(field, Command.Access.toList());
                                            }
                                        }
                                        else {
                                            permissionViolations += buildPermsVio(permissionViolations, field);
                                        }
                                    }
                                    else if (field.equalsIgnoreCase("visibility")) {
                                        if (c.getLevel().getWeight() < 300) {
                                            try {
                                                toEdit.setVisibility(value);
                                            }
                                            catch (IllegalArgumentException e) {
                                                paramErrors += buildError(field, Command.Visibility.toList());
                                            }
                                        }
                                        else {
                                            permissionViolations += buildPermsVio(permissionViolations, field);
                                        }
                                    }
                                    else if (field.equalsIgnoreCase("global")) {
                                        if (c.getLevel().getWeight() < 300) {
                                            try {
                                                toEdit.setGlobal(value);
                                            }
                                            catch (IllegalArgumentException e) {
                                                paramErrors += buildError(field,"true or false");
                                            }
                                        }
                                        else {
                                            permissionViolations += buildPermsVio(permissionViolations, field);
                                        }
                                    }
                                    else if (field.equalsIgnoreCase("delay")) {
                                        if (c.getLevel().getWeight() < 300) {
                                            try {
                                                toEdit.setDelay(value);
                                            }
                                            catch (Exception e) {
                                                paramErrors += buildError(field, e.getMessage());
                                            }
                                        }
                                        else {
                                            permissionViolations += buildPermsVio(permissionViolations, field);
                                        }
                                    }
                                    }
                            }

                            if (!permissionViolations.isEmpty()) {
                                permissionViolations += " fields.";
                            }

                            if (!badParams.isEmpty()) {
                                badParams += " are not formatted correctly or have an invalid value.";
                            }

                            paramErrors = badParams + paramErrors;

                            paramErrors += badField;
                        }
                        
                        channels.get(channel).getList().getCommands().remove(c.getName());
                        channels.get(channel).getList().getCommands().put(name, toEdit);

                        buildJson(channel);

                        sendMessage(channel, "Your command was edited successfully." + delayWarning + permissionViolations + paramErrors);
                    }//End Block 3 If
                    
                    else {//Start Block 3 Else
                        sendMessage(channel, "You do not have permission to edit this command.");
                    }//End Block 3 Else
                    
                }
                
                else {//Start Block 2 Else
                    sendMessage(channel, "The command " + name + " doesn't exist. If you were trying to add this command use \"!command add\" instead.");
                }//End Block 2 Else
                
            }//End Block 1 If
            
            else {//Start Block 1 Else
                if (parameters.startsWith("!")) {
                    sendMessage(channel, noOutput);
                }
                else {
                    sendMessage(channel, noName);
                }
            }//End Block 1 Else
        }
        
        public void handleCommandRemove(String channel, String sender, String name) {
            String formatRemind = " Make sure you use the format \"!command remove ![name]\".";
            String noName = "It looked like you tried to remove a command but I was unable to find a valid command name." + formatRemind;
            
            CommandList thisChannel = new CommandList();
            thisChannel.putCommands(natives);
            thisChannel.putCommands(channels.get(channel).getList());
            Command c = thisChannel.getCommands().get(name);
            
            if (name.startsWith("!") && !name.contains(" ")) { //Check that the name is valid
                
                if (c != null) {
                    
                    if (c.checkLevel(checkWeight(channel, sender))) { //Check that the command can be removed
                        channels.get(channel).getList().getCommands().remove(c.getName());

                        buildJson(channel);

                        sendMessage(channel, "Your command was removed successfully.");
                    }
                    
                    else {
                        sendMessage(channel, "You do not have permission to remove this command.");
                    }
                    
                }
                
                else {
                    sendMessage(channel, "The command " + name + " doesn't exist. If you were trying to add this command use \"!command add\" instead.");
                }
            }
            
            else {
                sendMessage(channel, noName);
            }
        }
        
}//End of class