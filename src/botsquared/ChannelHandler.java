package botsquared;

import java.util.concurrent.CopyOnWriteArraySet;

public class ChannelHandler {
    private BotSkeleton bot = null;
    private CopyOnWriteArraySet<Channel> channels = new CopyOnWriteArraySet<>();
    private CommandList natives = new CommandList();
    private CopyOnWriteArraySet<User> users = new CopyOnWriteArraySet<>();
    
    ChannelHandler (BotSkeleton bot) {
        this.bot = bot;
    }
    
    public void handleSubscriber(String channel, String user) {
        getChannel(channel).addSubscriber(user);
    }
    
    public void addUser(User user) {
        users.add(user);
    }
    
    public void removeUser(User user) {
        users.remove(user);
    }
    
    public CopyOnWriteArraySet getChannels() {
        return channels;
    }
    
    public Channel getChannel(String channel) {
        if (channel == null || channel.length() == 0) return null;
        if (!channel.startsWith("#")) channel = "#" + channel;
        for (Channel c : channels) {
            if (c.getName().equalsIgnoreCase(channel)) {
                return c;
            }
        }
        return null;
    }
    
    public CommandList getNatives() {
        return natives;
    }
    
    public User getUser(String name, boolean create) {
        for (User u : users) {
            if (u.getNick().equalsIgnoreCase(name)) {
                return u;
            }
        }
        if (create) {
            User u = new User(name);
            addUser(u);
            return u;
        }
        else {
            return null;
        }
    }
    
    public void setChannels(CopyOnWriteArraySet<Channel> channels) {
            this.channels = channels;
    }
    
    public void setNatives(CommandList natives) {
        this.natives = natives;
    }
    
    public void dispose() {
        channels.clear();
        users.clear();
    }
}
