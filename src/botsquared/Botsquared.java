package botsquared;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
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
public class Botsquared extends BotSkeleton {
    
        /**
         * Sets the nick of the bot.
         */
	public Botsquared() {
		this.setName("Botsquared");
	}
        
        /**
         * This method handles messages sent in chat.
         * 
         * @param channel
         * @param sender
         * @param message 
         */
    @Override
	public void onMessage(String channel, String sender, String message) {
            Channel ch = getChannelHandler().getChannel(channel);
            
            Matcher m = Util.pExactName.matcher(message);
            
            if (m.matches()) {
                CommandList thisChannel = new CommandList();
                thisChannel.putCommands(getChannelHandler().getNatives());
                thisChannel.putCommands(ch.getList());
                Command c = thisChannel.getCommands().get(message);
                
                if (c != null) {
                    handleCommand(channel, message, sender, c);
                }
                else {
                    String name = thisChannel.similar(message);
                    c = thisChannel.getCommands().get(name);
                    if (c != null) {
                        handleCommand(channel, message, sender, c);
                    }
                }
            }
            else if (message.startsWith("!") && message.contains(" ")) {
                Command c = getChannelHandler().getNatives().getCommands().get(message.substring(0, message.indexOf(" ")));
                
                message = message.replace("\n", "");

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
            else if (Util.isLink(message) && ch.getModerate().getLinks() && checkWeight(channel, sender) < Command.Access.SUB.getWeight()) {
                if (ch.getModerate().getPermitList().contains(sender.toLowerCase())) {
                    ch.getModerate().getPermitList().remove(sender);
                }
                else {
                    sendMessage(channel, "/timeout " + sender + " 1");//Timeout sender for 1 second to delete message
                    sendMessage(channel, "Please ask permissions before posting links " + sender + ".");
                }
            }
            
            else if (Util.isExcCaps(message) && ch.getModerate().getCaps() && checkWeight(channel, sender) < Command.Access.SUB.getWeight()) {
                sendMessage(channel, "/timeout " + sender + " 1");//Timeout sender for 1 second to delete message
                sendMessage(channel, "Please do not send messages with excessive caps " + sender + ".");
            }
	}
        
        @Override
        public void onNewSubscriber(String channel, String message, String user) {
            Channel c = getChannelHandler().getChannel(channel);
            String notify = c.getSubMessage();
            String subMessage = notify.replaceAll("<user>", user);
            sendMessage(channel, subMessage);
        }
        
        @Override
        public void onContinuedSubscriber(String channel, String months, String user) {
            sendMessage(channel, "Thanks " + user + " for subscribing for " + months + " months in a row!");
        }
        
        public int checkWeight(String channel, String sender) {
            Channel c = getChannelHandler().getChannel(channel);
            if (sender.equalsIgnoreCase("bearsquared")) {
                return Command.Access.BEARSQUARED.getWeight();
            }
            else if (c.isOwner(sender)) {
                return Command.Access.OWNER.getWeight();
            }
            else if (c.isMod(sender)) {
                return Command.Access.MOD.getWeight();
            }
            else if (c.isSubscriber(getChannelHandler().getUser(sender, true))) {
                return Command.Access.SUB.getWeight();
            }
            else {
                return 0;
            }
        }
        
        public void handleCommand(String channel, String message, String sender, Command c) {
            if (c.sendable(checkWeight(channel, sender), getChannelHandler().getChannel(channel).getGlobalTimeout(), getChannelHandler().getChannel(channel).getGlobalDelay())) {
                if (c.getLevel() != Command.Level.COMPLEX) {
                    if (c.getGlobal()) {
                        getChannelHandler().getChannel(channel).setGlobalTimeout(System.currentTimeMillis());
                    }

                    c.setLastUsed(System.currentTimeMillis());

                    sendMessage(channel, c.getOutput());
                }
                else {
                    if (c.getName().equalsIgnoreCase("!commands")
                            || c.getName().equalsIgnoreCase("!list") 
                            && c.sendable(checkWeight(channel, sender), getChannelHandler().getChannel(channel).getGlobalTimeout(), getChannelHandler().getChannel(channel).getGlobalDelay())) {
                        if (c.getGlobal()) {
                            getChannelHandler().getChannel(channel).setGlobalTimeout(System.currentTimeMillis());
                        }

                        c.setLastUsed(System.currentTimeMillis());

                        sendMessage(channel, getChannelHandler().getChannel(channel).getList().toList());
                    }
                    else if (c.getName().equalsIgnoreCase("!join") && channel.equalsIgnoreCase("#" + getName())) {
                        joinChannel("#" + sender);
                    }

                    else if (c.getName().equalsIgnoreCase("!leave") && c.checkAccess(checkWeight(channel, sender))) {
                        sendMessage(channel, c.getOutput());
                        partChannel(channel);
                    }

                    else if (c.getName().equalsIgnoreCase("!quote")) {
                        sendMessage(channel, getChannelHandler().getChannel(channel).getQuote());
                    }

                    else if (c.getName().equalsIgnoreCase("!uptime")) {
                        
                        StreamAPI stream = Util.getUptime(channel);
                        
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
            Channel c = getChannelHandler().getChannel(channel);
            String parameter = Util.splitAERMessage(message).getValue(1).toString();
            
            if (c.isMod(this.getName())) {
                if (parameter.contains("links") || parameter.contains("caps")) {
                    if (parameter.contains("links")) {
                        if (!c.getModerate().getLinks()) {
                            c.getModerate().setLinks(true);
                            sendMessage(channel, "I will now delete links.");
                        }
                        else {
                            c.getModerate().setLinks(false);
                            sendMessage(channel, "I will no longer delete links.");
                        }
                    }
                    if (parameter.contains("caps")) {
                        if (!c.getModerate().getCaps()) {
                            c.getModerate().setCaps(true);
                            sendMessage(channel, "I will now delete messages with excessive caps.");
                        }
                        else {
                            c.getModerate().setCaps(false);
                            sendMessage(channel, "I will no longer delete messages with excessive caps.");
                        }
                    }
                }
                else {
                    sendMessage(channel, "The command \"!moderate\" only accepts \"links\", and \"caps\" as parameters.");
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
            Channel c = getChannelHandler().getChannel(channel);
            
            if (c.getModerate().getLinks()) { //Check if moderate is true
                String parameter = Util.splitAERMessage(message).getValue(1).toString();

                if(!parameter.isEmpty() && parameter.length() > 0) {
                    if (!c.getModerate().getPermitList().contains(parameter)) {
                        sendMessage(channel, "You have permitted " + parameter + " to post one link.");
                        c.getModerate().getPermitList().add(parameter.toLowerCase());
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
            Channel c = getChannelHandler().getChannel(channel);
            String parameter = Util.splitAERMessage(message).getValue(1).toString();
            
            if (parameter.startsWith("open")) {
                parameter = parameter.replace("open", "").trim();
                if (parameter.contains(" ")) {
                    String[] options = parameter.split("\\|");
                    ArrayList<PollOption> poll = new ArrayList<>();
                    for (String option : options) {
                        poll.add(new PollOption(option.trim()));
                    }
                    sendMessage(channel, c.getPoll().open(poll));
                    buildJson(channel);
                }
                else {
                    sendMessage(channel, "Sorry, I didn't find any options for your poll.");
                }
            }
            else if (parameter.startsWith("close")) {
                sendMessage(channel, c.getPoll().close());
                buildJson(channel);
            }
            else if (parameter.startsWith("reset")) {
                sendMessage(channel, c.getPoll().reset());
                buildJson(channel);
            }
            else if (parameter.startsWith("results")) {
                sendMessage(channel, c.getPoll().results());
                buildJson(channel);
            }
        }
        
        public void handleQuote(String channel, String message) {
            Channel c = getChannelHandler().getChannel(channel);
            String parameter = Util.splitAERMessage(message).getValue(1).toString();
            
            if (!parameter.isEmpty() && parameter.length() > 0) {
                if (!parameter.startsWith("remove ")) {
                    c.getQuotes().add(parameter);
                    buildJson(channel);
                }
                else {
                    parameter = parameter.replace("remove", "").trim();
                    if (!parameter.isEmpty() && parameter.length() > 0) {
                        if (Util.isInteger(parameter)) {
                            if (Integer.parseInt(parameter) <= c.getQuotes().size()) {
                                c.getQuotes().remove(Integer.parseInt(parameter) - 1);
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
            Channel c = getChannelHandler().getChannel(channel);
            Tuple aer = Util.splitAERMessage(message);
            
            if (aer.getSize() == 3) {
                String type = aer.getValue(0).toString();
                String mode = aer.getValue(1).toString();
                String params = aer.getValue(2).toString();
                
                if (mode.equalsIgnoreCase("add")) {
                    Pair<Boolean, String> p1 = new Pair<>(false, "There was an error processing your request.");
                    if (type.equalsIgnoreCase("!command")) {
                        p1 = c.getList().addCommand(params);
                    }
                    else if (type.equalsIgnoreCase("!repeat")) {
                        p1 = c.getRepeatList().add(params);
                    }
                    
                    if (p1.getValue0()) {
                        buildJson(channel);
                    }
                    sendMessage(channel, p1.getValue1());
                }
                else if (mode.equalsIgnoreCase("edit")) {
                    Pair<Boolean, String> p1 = new Pair<>(false, "There was an error processing your request.");
                    if (type.equalsIgnoreCase("!command")) {
                        p1 = c.getList().editCommand(params, checkWeight(channel, sender));
                    }
                    else if (type.equalsIgnoreCase("!repeat")) {
                        //p1 = c.getList().addRepeat(params);
                    }
                    
                    if (p1.getValue0()) {
                        buildJson(channel);
                    }
                    sendMessage(channel, p1.getValue1());
                }
                else if (mode.equalsIgnoreCase("remove")) {
                    Pair<Boolean, String> p1 = new Pair<>(false, "There was an error processing your request.");
                    if (type.equalsIgnoreCase("!command")) {
                        p1 = c.getList().removeCommand(params, checkWeight(channel, sender));
                    }
                    else if (type.equalsIgnoreCase("!repeat")) {
                        p1 = c.getRepeatList().remove(params);
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
            Channel c = getChannelHandler().getChannel(channel);
            String parameter = Util.splitAERMessage(message).getValue(1).toString();
            
            if(!parameter.isEmpty() && parameter.length() > 0) {
                c.setSubMessage(parameter);
                buildJson(channel);
                sendMessage(channel, "You have set the subscriber notification message successfully.");
            }
        }
        
        public void handleVote(String channel, String sender, String message) throws IllegalArgumentException {
            Channel c = getChannelHandler().getChannel(channel);
            String parameter = Util.splitAERMessage(message).getValue(1).toString();
            
            try {
                if(!parameter.isEmpty() && parameter.length() > 0) {
                    c.getPoll().vote(sender, Integer.parseInt(parameter));
                }
            }
            catch (NumberFormatException e) {
                
            }
        }
        
}//End of class