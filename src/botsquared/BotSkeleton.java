
package botsquared;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.*;

public class BotSkeleton implements ReplyConstants {
    
    public static final String version = "0.0.1";
    
    // Connection
    private InputThread inputThread = null;
    private OutputThread outputThread = null;
    private String charset = null;
    private InetAddress inetAddress = null;
    
    // Server
    private String host = "irc.twitch.tv";
    private int port = 6667;
    private String password = null;
    
    // Outgoing
    private Queue outQueue = new Queue();
    private long messageDelay = 1000;
    
    private InetAddress dccInetAdress = null;
    
    // Config
    private boolean verbose = true;
    private String name = "Botsquared";
    private String nick = name;
    private String login = "Botsquared";
    private String versionString = "Botsquared " + version + " Java Twitch Bot.";
    
    private String channelPrefixes = "#&+!";
    
    //Channel
    private ChannelHandler channelHandler = new ChannelHandler();
    
    public BotSkeleton() {}
    
    public final synchronized void connect(String password) throws IOException, IrcException {
        this.password = password;
        
        if (isConnected()) {
            throw new IOException("The Bot is already connected to an IRC server.  Disconnect first.");
        }
        
        // Don't clear the outqueue - there might be something important in it!
        
        // Connect to the server.
        Socket socket = new Socket(host, port);
        this.log("*** Connected to server.");
        
        inetAddress = socket.getLocalAddress();
        
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        
        if (getEncoding() != null) {
            // Assume the specified encoding is valid for this JVM.
            inputStreamReader = new InputStreamReader(socket.getInputStream(), getEncoding());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), getEncoding());
        }
        else {
            // Otherwise, just use the JVM's default encoding.
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        }
        
        BufferedReader bReader = new BufferedReader(inputStreamReader);
        BufferedWriter bWriter = new BufferedWriter(outputStreamWriter);
        
        // Attempt to join the server.
        if (password != null && !password.equals("")) {
            OutputThread.sendRawLine(this, bWriter, "PASS " + password);
        }
        String nick = this.getName();
        OutputThread.sendRawLine(this, bWriter, "NICK " + nick);
        OutputThread.sendRawLine(this, bWriter, "USER " + this.getLogin() + " " + this.getVersion());
        
        inputThread = new InputThread(this, socket, bReader, bWriter);
        
        // Read stuff back from the server to see if we connected.
        String line = null;
        int tries = 1;
        try {
            while ((line = bReader.readLine()) != null) {
                this.handleLine(line);

                int firstSpace = line.indexOf(" ");
                int secondSpace = line.indexOf(" ", firstSpace + 1);
                if (secondSpace >= 0) {
                    String code = line.substring(firstSpace + 1, secondSpace);

                    if (code.equals("004")) {
                       // We're connected to the server.
                        break;
                    }
                    else if (code.equals("433")) {
                        socket.close();
                        inputThread = null;
                    }
                    else if (code.startsWith("5") || code.startsWith("4")) {
                        socket.close();
                        inputThread = null;
                    }
                }
            }
        } catch (Exception e) {
            try {
                socket.close();
            } catch (Exception ignored) {
                // Do nothing
            }
            inputThread = null;
        }
        
        this.log("*** Logged onto server.");
        
        //This makes the socket timeout on read operations after 5 minutes.
        socket.setSoTimeout(5 * 60 * 1000);
        
        inputThread.start();
        
        if (outputThread == null) {
            outputThread = new OutputThread(this, outQueue);
            outputThread.start();
            sendRawLine("CAP REQ :twitch.tv/commands");
            sendRawLine("CAP REQ :twitch.tv/membership");
            sendRawLine("CAP REQ :twitch.tv/tags");
        }
        
        buildNatives();
        
        this.onConnect();
    }
    
    /**
     * Reconnects to the IRC server that we were previously connected to.
     * If necessary, the appropriate port number and password will be used.
     * This method will throw an IrcException if we have never connected
     * to an IRC server previously.
     */
    public final synchronized void reconnect() throws IOException, IrcException {
        if (getHost() == null) {
            throw new IrcException("Cannot reconect to an IRC server because we were never connected to one previously!");
        }
        connect(getPassword());
    }
    
    /**
     * This method disconnects from the server cleanly by calling the
     * quitServer() method.  Providing the PircBot was connected to an
     * IRC server, the onDisconnect() will be called as soon as the
     * disconnection is made by the server.
     *
     * @see #quitServer() quitServer
     * @see #quitServer(String) quitServer
     */
    public final synchronized void disconnect() {
        this.quitServer();
    }
    
    /**
     * Joins a channel.
     * 
     * @param channel The name of the channel to join (eg "#cs").
     */
    public final void joinChannel(String channel) {
        this.sendRawLine("JOIN " + channel);
        buildChannel(channel);
    }
    
    /**
     * Parts a channel.
     *
     * @param channel The name of the channel to leave.
     */
    public final void partChannel(String channel) {
        sendRawLine("PART " + channel);
        channelHandler.getChannels().remove(channel);
    }
    
    /**
     * Quits from the IRC server.
     * Providing we are actually connected to an IRC server, the
     * onDisconnect() method will be called as soon as the IRC server
     * disconnects us.
     */
    public final void quitServer() {
        this.quitServer("");
    }
    
    /**
     * Quits from the IRC server with a reason.
     * Providing we are actually connected to an IRC server, the
     * onDisconnect() method will be called as soon as the IRC server
     * disconnects us.
     *
     * @param reason The reason for quitting the server.
     */
    public final void quitServer(String reason) {
        this.sendRawLine("QUIT :" + reason);
    }
    
    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     *
     * @param line The raw line to send to the IRC server.
     */
    public final synchronized void sendRawLine(String line) {
        if (isConnected()) {
            inputThread.sendRawLine(line);
        }
    }
    
    /**
     * Sends a raw line through the outgoing message queue.
     * 
     * @param line The raw line to send to the IRC server.
     */
    public final synchronized void sendRawLineViaQueue(String line) {
        if (line == null) {
            throw new NullPointerException("Cannot send null messages to the server.");
        }
        if (isConnected()) {
            outQueue.add(line);
        }
    }
    
    /**
     * Sends a message to a channel or a private message to a user.  These
     * messages are added to the outgoing message queue and sent at the
     * earliest possible opportunity.
     *  <p>
     * Some examples: -
     *  <pre>    // Send the message "Hello!" to the channel #cs.
     *    sendMessage("#cs", "Hello!");
     *    
     *    // Send a private message to Paul that says "Hi".
     *    sendMessage("Paul", "Hi");</pre>
     *  
     * You may optionally apply colours, boldness, underlining, etc to
     * the message by using the <code>Colors</code> class.
     *
     * @param target The name of the channel or user nick to send to.
     * @param message The message to send.
     * 
     * @see Colors
     */
    public final void sendMessage(String target, String message) {
        outQueue.add("PRIVMSG " + target + " :" + message);
        if (message.startsWith("/me")) {
            handleLine(":" + getNick() + "!" + getNick() + "@" + getNick() + ".tmi.twitch.tv PRIVMSG " + target + " :\u0001ACTION " + message.substring(4) + "\u0001");
        }
        else {
             handleLine(":" + getNick() + "!" + getNick() + "@" + getNick() + ".tmi.twitch.tv PRIVMSG " + target + " :" + message);
        }
    }
    
    /**
     * Adds a line to the log.  This log is currently output to the standard
     * output and is in the correct format for use by tools such as pisg, the
     * Perl IRC Statistics Generator.  You may override this method if you wish
     * to do something else with log entries.
     * Each line in the log begins with a number which
     * represents the logging time (as the number of milliseconds since the
     * epoch).  This timestamp and the following log entry are separated by
     * a single space character, " ".  Outgoing messages are distinguishable
     * by a log entry that has ">>>" immediately following the space character
     * after the timestamp.  DCC events use "+++" and warnings about unhandled
     * Exceptions and Errors use "###".
     *  <p>
     * This implementation of the method will only cause log entries to be
     * output if the PircBot has had its verbose mode turned on by calling
     * setVerbose(true);
     * 
     * @param line The line to add to the log.
     */
    public void log(String line) {
        if (verbose) {
            System.out.println(System.currentTimeMillis() + " " + line);
        }
    }
    
    /**
     * This method handles events when any line of text arrives from the server,
     * then calling the appropriate method in the PircBot.  This method is
     * protected and only called by the InputThread for this instance.
     *  <p>
     * This method may not be overridden!
     * 
     * @param line The raw line of text from the server.
     */
    protected void handleLine(String line) {
        this.log(line);
        
        // Check for server pings.
        if (line.startsWith("PING ")) {
            // Respond to the ping and return immediately.
            this.sendRawLine("PONG " + line.substring(5));
            return;
        }
        
        line = line.replaceAll("\\s+", " ");
        
        String sourceNick = "";
        String sourceLogin = "";
        String sourceHostname = "";
        
        StringTokenizer tokenizer = new StringTokenizer(line);
        String tags = null;
        String content = null;
        
        if (line.startsWith("@")) {
            tags = tokenizer.nextToken();
            if (line.contains("USERSTATE")) {
                this.parseUserstate(line);
                return;
            }
            else {
                content = line.substring(line.indexOf(" :", line.indexOf(" :") + 2) + 2);
            }
        }
        else {
            content = line.substring(line.indexOf(" :") + 2);
        }
        
        String senderInfo = tokenizer.nextToken();
        String command = tokenizer.nextToken();
        String target = null;
        
        if (checkCommand(command, tags, line, content, tokenizer)) return;
        
        int exclamation = senderInfo.indexOf("!");
        int at = senderInfo.indexOf("@");
        if (senderInfo.startsWith(":")) {
            if (exclamation > 0 && at > 0 && exclamation < at) {
                sourceNick = senderInfo.substring(1, exclamation);
                sourceLogin = senderInfo.substring(exclamation + 1, at);
                sourceHostname = senderInfo.substring(at + 1);
            }
            else {
                if (tokenizer.hasMoreTokens()) {
                    int code = -1;
                    try {
                        code = Integer.parseInt(command);
                    } catch (NumberFormatException e) {
                        // Keep the existing value.
                    }
                    if (code != -1) {
                        String response = line.substring(line.indexOf(command, senderInfo.length()) + 4, line.length());
                        this.processServerResponse(code, response);
                        // Return from the method.
                        return;
                    }
                    else {
                        // This is not a server response.
                        // It must be a nick without login and hostname.
                        // (or maybe a NOTICe or suchlike from the server)
                        sourceNick = senderInfo;
                        target = command;
                    }
                }
                else {
                    // We don't know what this line means.
                    onUnknown(line);
                    // Return from the method;
                    return;
                }
            }
        }
        
        if (sourceNick.startsWith(":")) {
            sourceNick = sourceNick.substring(1);
        }
        
        command = command.toUpperCase();
        
        if (target == null) {
            target = tokenizer.nextToken();
        }
        if (target.startsWith(":")) {
            target = target.substring(1);
        }
        
        parseTags(tags, sourceNick, target);
        
        // Check for CTCP requests.
        if (command.equals("PRIVMSG") && line.indexOf(":\u0001") > 0 && line.endsWith("\u0001")) {
            String request = line.substring(line.indexOf(":\u0001") + 2, line.length() - 1);
            if (request.startsWith("ACTION ")) {
                // ACTION request
                this.onAction(sourceNick, target, request.substring(7));
            }
        }
        else if (command.equals("PRIVMSG") && channelPrefixes.indexOf(target.charAt(0)) >= 0) {
            if (sourceNick.equalsIgnoreCase("jtv")) {
                this.onJTVMessage(target.substring(1), content);
                return;
            }
            if (sourceNick.equalsIgnoreCase("twitchnotify")) {
                if (line.contains("subscribed") || line.contains("subscribed for") && !line.contains("resubscribed")) {
                    String user = content.split(" ")[0];

                    channelHandler.handleSubscriber(target, user);

                    if (line.contains("subscribed")) {
                        this.onNewSubscriber(target, content, user);
                    }
                    else {
                        this.onContinuedSubscriber(target, content, user);
                    }
                }
                else if (line.contains("resubscribed")) {
                    onJTVMessage(target.substring(1), content);
                }
                return;
            }

            this.onMessage(target, sourceNick, content);
        }
        else if (command.equals("PRIVMSG")) {
            if (sourceNick.equals("jtv")) {
                if (line.contains("now hosting you")) {
                    this.onBeingHosted(content);
                }
            }

            // Private message to us.
            this.onPrivateMessage(sourceNick, sourceLogin, sourceHostname, content);
        }
        else if (command.equals("JOIN")) {
            // Someone is joining a channel.
            String channel = target;
            //this.addUser(channel, new User("", sourceNick));
            this.onJoin(channel, sourceNick, sourceLogin, sourceHostname);
        }
        else {
            // We don't know what to do with this.
            onUnknown(line);
        }
    }
    
    /**
     * This method is called once the PircBot has successfully connected to
     * the IRC server.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     */
    protected void onConnect() {}
    
    
    /**
     * This method carries out the actions to be performed when the PircBot
     * gets disconnected.  This may happen if the PircBot quits from the
     * server, or if the connection is unexpectedly lost.
     *  <p>
     * Disconnection from the IRC server is detected immediately if either
     * we or the server close the connection normally. If the connection to
     * the server is lost, but neither we nor the server have explicitly closed
     * the connection, then it may take a few minutes to detect (this is
     * commonly referred to as a "ping timeout").
     *  <p>
     * If you wish to get your IRC bot to automatically rejoin a server after
     * the connection has been lost, then this is probably the ideal method to
     * override to implement such functionality.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     */
    protected void onDisconnect() {}
    
    /**
     * This method is called by the PircBot when a numeric response
     * is received from the IRC server.  We use this method to
     * allow PircBot to process various responses from the server
     * before then passing them on to the onServerResponse method.
     *  <p>
     * Note that this method is private and should not appear in any
     * of the Javadoc generated documentation.
     * 
     * @param code The three-digit numerical code for the response.
     * @param response The full response from the IRC server.
     */
    private void processServerResponse(int code, String response) {
        if (code == 366) { // "END OF NAMES"
            int channelEndIndex = response.indexOf(" :");
            String channel = response.substring(response.lastIndexOf(' ', channelEndIndex - 1) + 1, channelEndIndex);
            sendMessage(channel, ".mods"); // Get mod list
        }
        onServerResponse(code, response);
    }
    
    private boolean checkCommand(String command, String tags, String line, String content, StringTokenizer tokenizer) {
        String target;
        switch (command) {
            case "CLEARCHAT":
                target = tokenizer.nextToken();
                this.onClearChat(target, (line.contains(" :")) ? content : null);
                return true;
            case "HOSTTARGET":
                target = tokenizer.nextToken();
                String[] split = content.split(" ");
                this.onHosting(target.substring(1), split[0], split[1]);
                return true;
            case "NOTICE":
                if (tags.contains("room_mods")) {
                    target = tokenizer.nextToken();
                    this.buildMods(target, content);
                    return true;
                }
                else if (!tags.contains("host_on") && !tags.contains("host_off")) {
                    target = tokenizer.nextToken();
                    this.onJTVMessage(target.substring(1), content);
                    return true;
                }   break;
            case "ROOMSTATE":
                target = tokenizer.nextToken();
                this.onRoomstate(target, tags);
                this.parseTags(tags, null, target);
            return true;
        }
        return false;
    }
    
    public void buildChannel(String channel) {
        try {
            File f = new File(channel + ".json");
            Gson gson = new Gson();

            //Read in channel commands
            if (f.exists() && f.length() != 0) {
                try (BufferedReader br2 = new BufferedReader(new FileReader(f))) {
                    Type channelType = new TypeToken<Channel>() {}.getType();
                    channelHandler.getChannels().add(gson.fromJson(br2, Channel.class));
                }
            }

            else {
                String json = gson.toJson(new Channel(channel));
                try {
                    try (FileWriter writer = new FileWriter(channel + ".json")) {
                        writer.write(json);
                        channelHandler.getChannels().add(new Channel(channel));
                        buildJson(channel);
                    }
                } catch (IOException e) {

                }
            }

        } catch (IOException e) {

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
        String json = gson.toJson(channelHandler.getChannel(channel));
        try {
            try (FileWriter writer = new FileWriter(channel + ".json")) {
                writer.write(json);
            }
        } catch (IOException e) {

        }
    }
    
    private void buildMods(String channel, String line) {
        if (!line.equals("")) {
            String init = line.substring(line.indexOf(":") + 1);
            String[] upMods = init.replaceAll(" ", "").split(",");
            channelHandler.getChannel(channel).addMods(upMods);
        }
    }
    
    private void buildNatives() {
        CommandList nativeList = new CommandList();
        
        nativeList.getCommands().put("!bot", new Command (
                "!bot", 
                Command.Level.NATIVE, 
                Command.Access.PUBLIC, 
                true, 
                30, 
                "Hello, I am Botsquared, a bot created by Bearsquared."
        ));
        nativeList.getCommands().put("!command", new Command (
                "!command", 
                Command.Level.NATIVE, 
                Command.Access.OWNER, 
                true, 
                30, 
                "Use this to handle custom commands. Type \"!command add|edit|remove\" to respectively add, edit, or remove commands."
        ));
        nativeList.getCommands().put("!commands", new Command (
                "!commands", 
                Command.Level.COMPLEX, 
                Command.Access.PUBLIC, 
                true, 
                30, 
                ""
        ));
        nativeList.getCommands().put("!join", new Command (
                "!join", 
                Command.Level.COMPLEX, 
                Command.Access.PUBLIC, 
                true, 
                30, 
                ""
        ));
        nativeList.getCommands().put("!leave", new Command (
                "!leave", 
                Command.Level.COMPLEX, 
                Command.Access.PUBLIC, 
                true, 
                30, 
                "I am leaving the channel."
        ));
        nativeList.getCommands().put("!list", new Command (
                "!list", 
                Command.Level.COMPLEX, 
                Command.Access.PUBLIC, 
                true, 
                30, 
                ""
        ));
        nativeList.getCommands().put("!moderate", new Command (
                "!moderate", 
                Command.Level.NATIVE, 
                Command.Access.MOD, 
                true, 
                30, 
                "If I'm am a moderator in I can help moderate the chat. Use the command \"!moderate links\" to have me delete links."
        ));
        nativeList.getCommands().put("!permit", new Command (
                "!permit", 
                Command.Level.NATIVE, 
                Command.Access.MOD, 
                false, 
                0, 
                "This command will allow a user to post one link to chat. Type \"!permit [name of user]\" to give them permission."
        ));
        nativeList.getCommands().put("!poll", new Command (
                "!poll", 
                Command.Level.NATIVE, 
                Command.Access.MOD, 
                false, 
                0, 
                "This command allows you to create a poll for your viewers in the chat. Use \"!poll open option1 | option2 | ...\" to open a poll, and \"!poll close\" to close the poll and see the results."
        ));
        nativeList.getCommands().put("!quote", new Command (
                "!quote", 
                Command.Level.COMPLEX, 
                Command.Access.SUB, 
                true, 
                30, 
                ""
        ));
        nativeList.getCommands().put("!repeat", new Command (
                "!repeat", 
                Command.Level.NATIVE, 
                Command.Access.MOD, 
                true, 
                30, 
                "Use this to handle custom repeating message. Type \"!repeat add|edit|remove <name>\" to respectively add, edit, or remove commands."
        ));
        nativeList.getCommands().put("!subnotify", new Command (
                "!subnotify", 
                Command.Level.NATIVE, 
                Command.Access.OWNER, 
                true, 
                30, 
                "Use \"!subnotify [your message here]\" to set a subscriber notification."
        ));
        nativeList.getCommands().put("!uptime", new Command (
                "!uptime", 
                Command.Level.COMPLEX, 
                Command.Access.PUBLIC, 
                true, 
                30, 
                " has been streaming for "
        ));
        nativeList.getCommands().put("!vote", new Command (
                "!vote", 
                Command.Level.COMPLEX, 
                Command.Access.PUBLIC, 
                false, 
                0, 
                ""
        ));
        
        channelHandler.setNatives(nativeList);
    }
    
    private void parseUserstate(String line) {
        String[] parts = line.split(" ");
        String tags = parts[0];
        String channel = parts[3];
        parseTags(tags, "user", channel);
    }
    
    private void parseTags(String line, String user, String channel) {
        if (line != null) {
            line = line.substring(1);
            String[] parts = line.split(";");
            for (String part : parts) {
                String[] tags = part.split("=");
                String key = tags[0].toLowerCase();
                if (tags.length <= 1) continue;
                String value = tags[1];
                switch (key.toLowerCase()) {
                    case "color":
                        //this.handleColor(value, user);
                        break;
                    case "display-name":
                        this.handleDisplayName(value, user);
                        break;
                    case "emotes":
                        this.handleEmotes(value, user);
                        break;
                    case "subscriber":
                        if (value.equals("1")) {
                            this.handleSpecial(channel, key, user);
                        }
                        break;
                    case "turbo":
                        if (value.equals("1")) {
                            this.handleSpecial(channel, value, user);
                        }
                        break;
                    case "user-type":
                        this.handleSpecial(channel, value, user);
                        break;
                    case "native-lang":
                        // TODO once implemented
                        break;
                    case "r9k":
                        if (value.equals("1")) {
                            this.onJTVMessage(channel, "This room is in r9k mode.");
                        }
                        break;
                    case "slow":
                        if (!value.equals("0")) {
                            this.onJTVMessage(channel,
                                    "This room is in slow mode. You may send message ever " + value + " seconds.");
                        }
                        break;
                    case "subs-only":
                        if (value.equals("1")) {
                            this.onJTVMessage(channel,
                                    "This room is in subscribers-only mode.");
                        }
                        break;
                    case "emote-sets":
                        // Handle emotes
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    /**
     * This method is called when we receive a numeric response from the
     * IRC server.
     *  <p> 
     * Numerics in the range from 001 to 099 are used for client-server
     * connections only and should never travel between servers.  Replies
     * generated in response to commands are found in the range from 200
     * to 399.  Error replies are found in the range from 400 to 599.
     *  <p>
     * For example, we can use this method to discover the topic of a
     * channel when we join it.  If we join the channel #test which
     * has a topic of &quot;I am King of Test&quot; then the response
     * will be &quot;<code>PircBot #test :I Am King of Test</code>&quot;
     * with a code of 332 to signify that this is a topic.
     * (This is just an example - note that overriding the
     * <code>onTopic</code> method is an easier way of finding the
     * topic for a channel). Check the IRC RFC for the full list of other
     * command response codes.
     *  <p>
     * PircBot implements the interface ReplyConstants, which contains
     * contstants that you may find useful here.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     * 
     * @param code The three-digit numerical code for the response.
     * @param response The full response from the IRC server.
     * 
     * @see ReplyConstants
     */
    protected void onServerResponse(int code, String response) {}
    
    
    /**
     * This method is called whenever a message is sent to a channel.
     * <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel to which the message was sent.
     * @param sender  The nick of the person who sent the message.
     * @param message The actual message sent to the channel.
     */
    public void onMessage(String channel, String sender, String message) {}
    
    /**
     * This method catches the new subscriber for a certain channel
     * that the user "twitchnotify" sends to the channel when
     * the recipient (newSub) subscribes to the channel.
     *
     * @param channel The channel the new subscriber subscribed to.
     * @param line    The line to parse.
     * @param newSub  The nick of the user.
     */
    public void onNewSubscriber(String channel, String line, String newSub) {
    }
    
    public void onContinuedSubscriber(String channel, String line, String newSub) {
    }
    
    /**
     * This method is called whenever a private message is sent to the PircBot.
     * <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     * <p>
     * This is used when JTV sends its messages about who's op, subscriber, etc.
     *
     * @param sender   The nick of the person who sent the private message.
     * @param login    The login of the person who sent the private message.
     * @param hostname The hostname of the person who sent the private message.
     * @param message  The actual message.
     */
    public void onPrivateMessage(String sender, String login, String hostname, String message) {
    }
    
    /**
     * This is called when the chat is cleared. This is used for a crude
     * ban detection, or just the chat being cleared in general.
     * <p>
     * This can return "CLEARCHAT user" or "CLEARCHAT"
     *
     * @param channel The channel
     * @param line The "CLEARCHAT" line.
     */
    public void onClearChat(String channel, String line) {
    }
    
    /**
     * This method is called whenever an ACTION is sent from a user.  E.g.
     * such events generated by typing "/me goes shopping" in most IRC clients.
     * <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sender The nick of the user that sent the action.
     * @param target The target of the action, be it a channel or our nick.
     * @param action The action carried out by the user.
     */
    public void onAction(String sender, String target, String action) {
    }
    
    public void onHosting(String channel, String target, String count) {
    }

    /**
     * This method is called if you're being hosted by somebody.
     *
     * @param line The line from JTV.
     */
    public void onBeingHosted(String line) {
    }
    
    /**
     * This method is called whenever someone (possibly us) joins a channel
     * which we are on.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel which somebody joined.
     * @param sender The nick of the user who joined the channel.
     * @param login The login of the user who joined the channel.
     * @param hostname The hostname of the user who joined the channel.
     */
    protected void onJoin(String channel, String sender, String login, String hostname) {}

    /**
     * This method is called if JTV is trying to tell you something.
     *
     * @param channel The channel (used for getting the pane)
     * @param line    What JTV is trying to tell you.
     */
    public void onJTVMessage(String channel, String line) {
    }

    /**
     * Called when the state of a room is changing.
     *
     * @param channel The channel the ROOMSTATE is for.
     * @param tags    The tags (what is changing).
     */
    public void onRoomstate(String channel, String tags) {
    }
    
    /**
     * This method is called whenever we receive a line from the server that
     * the PircBot has not been programmed to recognise.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param line The raw line that was received from the server.
     */
    protected void onUnknown(String line) {
        // And then there were none :)
    }
    
    /**
     * Called when the mode of a user is set.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     * 
     * @since PircBot 1.2.0
     * 
     * @param targetNick The nick that the mode operation applies to.
     * @param sourceNick The nick of the user that set the mode.
     * @param sourceLogin The login of the user that set the mode.
     * @param sourceHostname The hostname of the user that set the mode.
     * @param mode The mode that has been set.
     * 
     */
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {}
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof BotSkeleton) {
            BotSkeleton other = (BotSkeleton) o;
            return this.getNick().equals(other.getNick()) && this.getPassword().equals(other.getPassword());
        }
        return false;
    }
    
    /**
     * Returns the hashCode of this PircBot. This method can be called by hashed
     * collection classes and is useful for managing multiple instances of
     * PircBots in such collections.
     *
     * @return the hash code for this instance of PircBot.
     * @since PircBot 0.9.9
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    /**
     * Returns a String representation of this object.
     * You may find this useful for debugging purposes, particularly
     * if you are using more than one PircBot instance to achieve
     * multiple server connectivity. The format of
     * this String may change between different versions of PircBot
     * but is currently something of the form
     * <code>
     * Version{PircBot x.y.z Java IRC Bot - www.jibble.org}
     * Connected{true}
     * Server{irc.dal.net}
     * Port{6667}
     * Password{}
     * </code>
     *
     * @return a String representation of this object.
     * @since PircBot 0.9.10
     */
    public String toString() {
        return "Version{" + version + "}" +
                " Connected{" + isConnected() + "}" +
                " Server{" + host + "}" +
                " Port{" + port + "}" +
                " Password{" + password + "}";
    }
    
    public void dispose() {
        if (outputThread != null) outputThread.interrupt();
        if (inputThread != null) inputThread.dispose();
    }
    
    /**
     * Determines if a user is admin, staff, turbo, or subscriber and sets their prefix accordingly.
     *
     * @param channel The channel.
     * @param type
     * @param user
     */
    public void handleSpecial(String channel, String type, String user) {
        if (user != null) {
            Channel c = channelHandler.getChannel(channel);
            switch (type) {
                case "mod":
                    if (c != null) c.addMods(user);
                    break;
                case "subscriber":
                    if (c != null) c.addSubscriber(user);
                    break;
                case "turbo":
                    channelHandler.getUser(user, true).setTurbo(true);
                    break;
                case "admin":
                    channelHandler.getUser(user, true).setAdmin(true);
                    break;
                case "global_mod":
                    channelHandler.getUser(user, true).setGlobalMod(true);
                    break;
                case "staff":
                    channelHandler.getUser(user, true).setStaff(true);
                    break;
                default:
                    break;
            }
        }
    }
    
    public void handleDisplayName(String name, String user) {
        if (name != null) {
            channelHandler.getUser(user, true).setDisplayName(name.replaceAll("\\\\s", " ").trim());
        }
    }
    
    public void handleEmotes(String numbers, String user) {
        try {
            String[] parts = numbers.split("/");
            User u = channelHandler.getUser(user, true);
            for (String emote : parts) {
                String emoteID = emote.split(":")[0];
                try {
                    int id = Integer.parseInt(emoteID);
                    u.addEmote(id);
                } catch (Exception e) {
                    this.log("Cannot parse emote ID given by IRCv3 tags!");
                }
            }
        } catch (Exception e) {
            // Do nothing
        }
    }
    
    public final ChannelHandler getChannelHandler() {
        return channelHandler;
    }
    
    /**
     * Returns an array of all channels that we are in.  Note that if you
     * call this method immediately after joining a new channel, the new
     * channel may not appear in this array as it is not possible to tell
     * if the join was successful until a response is received from the
     * IRC server.
     *
     */
    public final void /*String[]*/ getChannels() {
        // Channel management
    }
    
    /**
     * Returns the encoding used to send and receive lines from
     * the IRC server, or null if not set.  Use the setEncoding
     * method to change the encoding charset.
     * 
     * @since PircBot 1.0.4
     * 
     * @return The encoding used to send outgoing messages, or
     *         null if not set.
     */
    public String getEncoding() {
        return charset;
    }
    
    /**
     * Returns the name of the last IRC server the PircBot tried to connect to.
     * This does not imply that the connection attempt to the server was
     * successful (we suggest you look at the onConnect method).
     * A value of null is returned if the PircBot has never tried to connect
     * to a server.
     *
     * @return The name of the last machine we tried to connect to. Returns
     * null if no connection attempts have ever been made.
     */
    public final String getHost() {
        return host;
    }
    
    /**
     * Returns the InetAddress used by the PircBot.
     * This can be used to find the I.P. address from which the PircBot is
     * connected to a server.
     *
     * @return The current local InetAddress, or null if never connected.
     * @since PircBot 1.4.4
     */
    public InetAddress getInetAddress() {
        return inetAddress;
    }
    
    /**
     * Gets the internal login of the Bot.
     *
     * @return The login of the Bot.
     */
    public final String getLogin() {
        return login;
    }
    
    /**
     * Gets the maximum length of any line that is sent via the IRC protocol.
     * The IRC RFC specifies that line lengths, including the trailing \r\n
     * must not exceed 512 bytes.  Hence, there is currently no option to
     * change this value in PircBot.  All lines greater than this length
     * will be truncated before being sent to the IRC server.
     * 
     * @return The maximum line length (currently fixed at 512)
     */
    public final int getMaxLineLength() {
        return InputThread.MAX_LINE_LENGTH;
    }
    
    /**
     * Returns the number of milliseconds that will be used to separate
     * consecutive messages to the server from the outgoing message queue.
     *
     * @return Number of milliseconds.
     */
    public final long getMessageDelay() {
        return messageDelay;
    }
    
    /**
     * Sets the number of milliseconds to delay between consecutive
     * messages when there are multiple messages waiting in the
     * outgoing message queue.  This has a default value of 1000ms.
     * It is a good idea to stick to this default value, as it will
     * prevent your bot from spamming servers and facing the subsequent
     * wrath!  However, if you do need to change this delay value (<b>not
     * recommended</b>), then this is the method to use.
     *
     * @param delay The number of milliseconds between each outgoing message.
     */
    public final void setMessageDelay(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Cannot have a negative time.");
        }
        messageDelay = delay;
    }
    
    /**
     * Gets the name of the Bot. This is the name that will be used as
     * as a nick when we try to join servers.
     *
     * @return The name of the Bot.
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns the current nick of the bot. Note that if you have just changed
     * your nick, this method will still return the old nick until confirmation
     * of the nick change is received from the server.
     *  <p>
     * The nick returned by this method is maintained only by the Bot
     * class and is guaranteed to be correct in the context of the IRC server.
     * 
     * @return The current nick of the bot.
     */
    public final String getNick() {
        return nick;
    }
    
    /**
     * Gets the number of lines currently waiting in the outgoing message Queue.
     * If this returns 0, then the Queue is empty and any new message is likely
     * to be sent to the IRC server immediately.
     *
     * @return The number of lines in the outgoing message Queue.
     * @since PircBot 0.9.9
     */
    public final int getOutgoingQueueSize() {
        return outQueue.size();
    }
    
    /**
     * Returns the port number of the last IRC server that the PircBot tried
     * to connect to.
     * This does not imply that the connection attempt to the server was
     * successful (we suggest you look at the onConnect method).
     * A value of -1 is returned if the PircBot has never tried to connect
     * to a server.
     *
     * @return The port number of the last IRC server we connected to.
     * Returns -1 if no connection attempts have ever been made.
     * @since PircBot 0.9.9
     */
    public final int getPort() {
        return port;
    }
    
    /**
     * Returns the last password that we used when connecting to an IRC server.
     * This does not imply that the connection attempt to the server was
     * successful (we suggest you look at the onConnect method).
     * A value of null is returned if the PircBot has never tried to connect
     * to a server using a password.
     *
     * @return The last password that we used when connecting to an IRC server.
     * Returns null if we have not previously connected using a password.
     * @since PircBot 0.9.9
     */
    public final String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Sets the verbose mode. If verbose mode is set to true, then log entries
     * will be printed to the standard output. The default value is false and
     * will result in no output. For general development, we strongly recommend
     * setting the verbose mode to true.
     *
     * @param verbose true if verbose mode is to be used.  Default is false.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    /**
     * Gets the internal version of the Bot.
     *
     * @return The version of the Bot.
     */
    public final String getVersion() {
        return versionString;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Sets the internal version of the Bot.  This should be set before joining
     * any servers.
     *
     * @param version The new version of the Bot.
     */
    public void setVersion(String version) {
        versionString = version;
    }
    
    /**
     * Returns whether or not the Bot is currently connected to a server.
     * The result of this method should only act as a rough guide,
     * as the result may not be valid by the time you act upon it.
     *
     * @return True if and only if the Bot is currently connected to a server.
     */
    public final synchronized boolean isConnected() {
        return inputThread != null && inputThread.isConnected();
    }
}
