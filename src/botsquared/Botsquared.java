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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.javatuples.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;

/**
 * Creates, reads, updates, removes and handles chat commands.
 * 
 */
public class Botsquared extends PircBot {
    
    private Map<String, Channel> channels = new HashMap<>();
    
    CommandList natives = new CommandList();
    
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
        public synchronized void initializeBot(String channel) throws FileNotFoundException, UnsupportedEncodingException, CloneNotSupportedException {
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
                    }
                }
                
                else {
                    String json = gson.toJson(new Channel(channel));
                    try {
                        try (FileWriter writer = new FileWriter(channel + ".json")) {
                            writer.write(json);
                            channels.put(channel, new Channel(channel));
                            buildJson(channel);
                        }
                    } catch (IOException e) {

                    }
                }
                
            } catch (IOException e) {
                
            }
            sendMessage(channel, ".mods");
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
                if (message.contains("SPECIALUSER")) {
                    String[] words = message.split(" ");
                    if (words[2].equalsIgnoreCase("subscriber") || words[2].equalsIgnoreCase("turbo") || words[2].equalsIgnoreCase("staff")) {
                        if (!channels.get(channel).getSuperList().contains(words[1])) {
                            channels.get(channel).getSuperList().add(words[1]);
                        }
                        buildJson(channel);
                    }
                }
                else if (message.contains("The moderators of this room are:")) {
                    String mods = message.substring(message.indexOf(":") + 1);
                    ArrayList<String> list = new ArrayList<>(Arrays.asList(mods.replaceAll(" ", "").split(",")));
                    channels.get(channel).setModList(list);
                }
            }
            
            // Subscriber notification
            else if (sender.equalsIgnoreCase("twitchnotify")) {
                String[] words = message.split(" ");
                if (words[2].equalsIgnoreCase("subscribed!")) {
                    String notify = channels.get(channel).getSubMessage();
                    String subMessage = notify.replaceAll("<user>", words[0]);
                    sendMessage(channel, subMessage);
                }
                else if (words[1].equalsIgnoreCase("subscribed")) {
                    sendMessage(channel, "Thanks " + words[0] + " for subscribing for " + words[3] + " months in a row!");
                }
            }
            
            Matcher m = Util.pExactName.matcher(message);
            
            //if (message.startsWith("!")) { // Messages that start with "!" are assumed to be a command.
            if (m.matches()) {
                CommandList thisChannel = new CommandList();
                thisChannel.putCommands(natives);
                thisChannel.putCommands(channels.get(channel).getList());
                Command c = thisChannel.getCommands().get(message);
                
                if (c != null) {
                    handleCommand(channel, message, sender, c);
                }
                else {
                    String name = channels.get(channel).getList().similar(message);
                    c = thisChannel.getCommands().get(name);
                    if (c != null) {
                        handleCommand(channel, message, sender, c);
                    }
                }
            }
            else if (message.contains(" ")) {
                Command c = natives.getCommands().get(message.substring(0, message.indexOf(" ")));

                if (c != null && c.checkAccess(checkWeight(channel, sender))) {
                    if (c.getName().equalsIgnoreCase("!command")) {
                        handleAER(channel, message, sender);
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

                    else if (c.getName().equalsIgnoreCase("!quote")) {
                        handleQuote(channel, message);
                    }

                    else if (c.getName().equalsIgnoreCase("!repeat")) {
                        handleAER(channel, message, sender);
                    }

                    else if (c.getName().equalsIgnoreCase("!subnotify")) {
                        handleSubNotify(channel, message);
                    }

                    else if (c.getName().equalsIgnoreCase("!vote")) {
                        handleVote(channel, sender, message);
                    }
                }
            }
            // If moderate is true then it will also check messages sent by normal users for URLs.
            else if (Util.isLink(message) && channels.get(channel).getModerate() && checkWeight(channel, sender) < Command.Access.SUB.getWeight()) {
                if (channels.get(channel).getPermitList().contains(sender.toLowerCase())) {
                    channels.get(channel).getPermitList().remove(sender);
                }
                else {
                    sendMessage(channel, "/timeout " + sender + " 1");//Timeout sender for 1 second to delete message
                    sendMessage(channel, "Please ask permissions before posting links " + sender + ".");
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
        
        public static boolean isInteger(String s) {
            return isInteger(s,10);
        }

        public static boolean isInteger(String s, int radix) {
            if(s.isEmpty()) return false;
            for(int i = 0; i < s.length(); i++) {
                if(i == 0 && s.charAt(i) == '-') {
                    if(s.length() == 1) return false;
                    else continue;
                }
                if(Character.digit(s.charAt(i),radix) < 0) return false;
            }
            return true;
        }
        
        public boolean isSub(String channel, String sender) {
            return channels.get(channel).getSuperList().contains(sender);
        }
        
        /**
         * Check the list of known moderators for the channel and return if the sender is found.
         * 
         * @param channel
         * @param sender
         * @return 
         */
        public boolean isMod(String channel, String sender) { 
            return channels.get(channel).getModList().stream().anyMatch((u) -> (u.equalsIgnoreCase(sender))) || isOwner(channel, sender) || sender.equalsIgnoreCase("bearsquared");
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
                return Command.Access.BEARSQUARED.getWeight();
            }
            else if (isOwner(channel, sender)) {
                return Command.Access.OWNER.getWeight();
            }
            else if (isMod(channel, sender)) {
                return Command.Access.MOD.getWeight();
            }
            else if (isSub(channel, sender)) {
                return Command.Access.SUB.getWeight();
            }
            else {
                return 0;
            }
        }
        
        public void handleCommand(String channel, String message, String sender, Command c) {
            if (c.sendable(checkWeight(channel, sender), channels.get(channel).getGlobalTimeout(), channels.get(channel).getGlobalDelay())) {
                if (c.getLevel() != Command.Level.COMPLEX) {
                    if (c.getGlobal()) {
                        channels.get(channel).setGlobalTimeout(System.currentTimeMillis());
                    }

                    c.setLastUsed(System.currentTimeMillis());

                    sendMessage(channel, c.getOutput());
                }
                else {
                    if (c.getName().equalsIgnoreCase("!commands")
                            || c.getName().equalsIgnoreCase("!list") 
                            && c.sendable(checkWeight(channel, sender), channels.get(channel).getGlobalTimeout(), channels.get(channel).getGlobalDelay())) {
                        if (c.getGlobal()) {
                            channels.get(channel).setGlobalTimeout(System.currentTimeMillis());
                        }

                        c.setLastUsed(System.currentTimeMillis());

                        sendMessage(channel, channels.get(channel).getList().toList());
                    }
                    else if (c.getName().equalsIgnoreCase("!join") && channel.equalsIgnoreCase("#" + getName())) {
                        joinChannel("#" + sender);
                    }

                    else if (c.getName().equalsIgnoreCase("!leave") && c.checkAccess(checkWeight(channel, sender))) {
                        sendMessage(channel, c.getOutput());
                        partChannel(channel);
                    }

                    else if (c.getName().equalsIgnoreCase("!quote")) {
                        sendMessage(channel, channels.get(channel).getQuote());
                    }

                    else if (c.getName().equalsIgnoreCase("!uptime")) {
                        String html = "https://api.twitch.tv/kraken/streams/" + channel.replaceFirst("#", "");

                        try {
                            String json = Jsoup.connect(html).ignoreContentType(true).execute().body();
                            Gson gson = new Gson();
                            StreamAPI stream = gson.fromJson(json, StreamAPI.class);
                            if (stream.getStream() != null) {
                                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                                DateTime created = fmt.parseDateTime(stream.getStream().getCreated_At());
                                created = created.withZoneRetainFields(DateTimeZone.UTC);

                                DateTime dt = new DateTime();

                                Period uptime = new Period(created,dt);

                                sendMessage(channel, channel.replaceFirst("#", "")
                                        + c.getOutput()
                                        + String.format("%d hours, %d minutes, and %d seconds.", uptime.getHours(), uptime.getMinutes(), uptime.getSeconds()));
                            }
                            else {
                                sendMessage(channel, "The channel is not online.");
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Botsquared.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
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
            String parameter = Util.splitMessage(message).getValue(1).toString();
            if (isMod(channel, this.getName())) {
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
                String parameter = Util.splitMessage(message).getValue(1).toString();

                if(!parameter.isEmpty() && parameter.length() > 0) {
                    if (!channels.get(channel).getPermitList().contains(parameter)) {
                        sendMessage(channel, "You have permitted " + parameter + " to post one link.");
                        channels.get(channel).getPermitList().add(parameter.toLowerCase());
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
            String parameter = Util.splitMessage(message).getValue(1).toString();
            
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
        
        public void handleQuote(String channel, String message) {
            String parameter = Util.splitMessage(message).getValue(1).toString();
            
            if (!parameter.isEmpty() && parameter.length() > 0) {
                if (!parameter.startsWith("remove ")) {
                    channels.get(channel).getQuotes().add(parameter);
                    buildJson(channel);
                }
                else {
                    parameter = parameter.replace("remove", "").trim();
                    if (!parameter.isEmpty() && parameter.length() > 0) {
                        if (isInteger(parameter)) {
                            if (Integer.parseInt(parameter) <= channels.get(channel).getQuotes().size()) {
                                channels.get(channel).getQuotes().remove(Integer.parseInt(parameter) - 1);
                                sendMessage(channel, "The quote has been removed.");
                                buildJson(channel);
                            }
                            else {
                                sendMessage(channel, "There is no quote #" + Integer.parseInt(parameter) + ".");
                            }
                        }
                        else {
                            sendMessage(channel, "Please enter the number of the quote you want to remove.");
                        }
                    }
                }
            }
        }
        
        public void handleAER(String channel, String message, String sender) {
            Tuple aer = Util.splitMessage(message);
            
            if (aer.getSize() == 3) {
                String type = aer.getValue(0).toString();
                String mode = aer.getValue(1).toString();
                String params = aer.getValue(2).toString();
                
                if (mode.equalsIgnoreCase("add")) {
                    Pair<Boolean, String> p1 = new Pair<>(false, "There was an error processing your request.");
                    if (type.equalsIgnoreCase("!command")) {
                        p1 = channels.get(channel).getList().addCommand(params);
                    }
                    else if (type.equalsIgnoreCase("!repeat")) {
                        //p1 = channels.get(channel).getList().addRepeat(params);
                    }
                    
                    if (p1.getValue0()) {
                        buildJson(channel);
                    }
                    sendMessage(channel, p1.getValue1());
                }
                else if (mode.equalsIgnoreCase("edit")) {
                    Pair<Boolean, String> p1 = new Pair<>(false, "There was an error processing your request.");
                    if (type.equalsIgnoreCase("!command")) {
                        p1 = channels.get(channel).getList().editCommand(params, checkWeight(channel, sender));
                    }
                    else if (type.equalsIgnoreCase("!repeat")) {
                        //p1 = channels.get(channel).getList().addRepeat(params);
                    }
                    
                    if (p1.getValue0()) {
                        buildJson(channel);
                    }
                    sendMessage(channel, p1.getValue1());
                }
                else if (mode.equalsIgnoreCase("remove")) {
                    Pair<Boolean, String> p1 = new Pair<>(false, "There was an error processing your request.");
                    if (type.equalsIgnoreCase("!command")) {
                        p1 = channels.get(channel).getList().removeCommand(params, checkWeight(channel, sender));
                    }
                    else if (type.equalsIgnoreCase("!repeat")) {
                        //p1 = channels.get(channel).getList().addRepeat(params);
                    }
                    
                    if (p1.getValue0()) {
                        buildJson(channel);
                    }
                    sendMessage(channel, p1.getValue1());
                }
            }
            else if (aer.getSize() == 2) {
                String type = aer.getValue(0).toString();
                String mode = aer.getValue(1).toString();
                
                if (mode.equalsIgnoreCase("add") || mode.equalsIgnoreCase("edit") || mode.equalsIgnoreCase("remove")) {
                    String output = "To <mode> a command use the format \"<type> <mode> ![name]".replaceAll("<mode>", mode).replace("<type>",type);
                    if (mode.equalsIgnoreCase("add") || mode.equalsIgnoreCase("edit")) {
                        output += " [output]";
                    }
                    output += "\".";

                    sendMessage(channel, output);
                }
                
                else {
                    String output = "I didn't recognize the parameter after <type>.".replace("<type>", type);
                    sendMessage(channel, output);
                }
            }
        }
        
        public void handleSubNotify(String channel, String message) {
            String parameter = Util.splitMessage(message).getValue(1).toString();
            
            if(!parameter.isEmpty() && parameter.length() > 0) {
                channels.get(channel).setSubMessage(parameter);
                buildJson(channel);
                sendMessage(channel, "You have set the subscriber notification message successfully.");
            }
        }
        
        public void handleVote(String channel, String sender, String message) throws IllegalArgumentException {
            String parameter = Util.splitMessage(message).getValue(1).toString();
            
            try {
                if(!parameter.isEmpty() && parameter.length() > 0) {
                    channels.get(channel).getPoll().vote(sender, Integer.parseInt(parameter));
                }
            }
            catch (NumberFormatException e) {
                
            }
        }
        
}//End of class