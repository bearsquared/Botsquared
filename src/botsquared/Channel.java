package botsquared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Channel {
    private transient BotSkeleton bot = null;
    private String name;
    private CommandList list = new CommandList();
    private int globalDelay = 5;
    private transient long globalTimeout = 0L;
    private Moderate moderate = new Moderate();
    private String subMessage = "(not set)";
    private ArrayList<String> quotes = new ArrayList<>();
    private RepeatList repeatList = null;
    private transient CopyOnWriteArrayList<String> mods = new CopyOnWriteArrayList<>();
    private transient CopyOnWriteArrayList<String> subs = new CopyOnWriteArrayList<>();
    private transient Poll poll = new Poll();
    
    private transient boolean canSend = true;
    private transient int mslr = 0;
    
    Channel() {
        
    }
    
    Channel(String name) {
        this.name = name;
    }
    
    public void addMods(String... mods) {
        Collections.addAll(this.mods, mods);
    }
    
    public void addSubscriber(String user) {
        if (!isSubscriber(user)) {
            subs.add(user);
        }
    }
    
    public void removeSubscriber(String user) {
        if (isSubscriber(user)) {
            subs.remove(user);
        }
    }
    
     /**
     * Checks to see if a given user is a moderator of a channel.
     *
     * @param user The user to check.
     * @return True if the user is a mod, else false.
     */
    public boolean isMod(String user) {
        for (String s : mods) {
            if (user.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
    
    /**
    * Checks if the sender is the owner of the channel.
    * 
    * @param user
    * @return True if the user is the owner, else false.
    */
   public boolean isOwner(String user) {
        String o;
        o = name.replace("#", "");
        return o.equalsIgnoreCase(user);
   }
    
    /**
     * Checks to see if a given user is a subscriber to a channel.
     *
     * @param user
     * @return True if the user is a subscriber, else false.
     */
    public boolean isSubscriber(User user) {
        for (String s : subs) {
            if (s.equals(user.getNick().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isSubscriber(String user) {
        for (String s : subs) {
            if (s.equals(user.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    public void checkRepeat() {
        if (!repeatList.getList().isEmpty()) {
            if (!canSend) {
                mslr++;
                if (mslr >= 30) {
                    canSend = true;
                    mslr = 0;
                    repeatList.consume();
                }
            }
            else {
                repeatList.consume();
            } 
        }
    }
    
    public String getName() {
        return name;
    }
    
    public CommandList getList() {
        return list;
    }
    
    public int getGlobalDelay() {
        return globalDelay;
    }
    
    public long getGlobalTimeout() {
        return globalTimeout;
    }
    
    public Moderate getModerate() {
        return moderate;
    }
    
    public String getSubMessage() {
        return subMessage;
    }
    
    public String getQuote() {
        if (!quotes.isEmpty()) {
            Random randGen = new Random();
            int n = randGen.nextInt(quotes.size());
            
            return "#" + (n + 1) + ": \"" + quotes.get(n) + "\"";
        }
        else {
            return "I do not know any quotes.";
        }
    }
    
    public ArrayList<String> getQuotes() {
        return quotes;
    }
    
    public RepeatList getRepeatList() {
        return repeatList;
    }
    
    public CopyOnWriteArrayList<String> getModList() {
        return mods;
    }
    
    public CopyOnWriteArrayList<String> getSubscriberList() {
        return subs;
    }
    
    public Poll getPoll() {
        return poll;
    }
    
    public boolean getCanSend() {
        return canSend;
    }
    
    public void setBot(BotSkeleton bot) {
        this.bot = bot;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setList(CommandList list) {
        this.list = list;
    }
    
    public void setGlobalDelay(int i) {
        globalDelay = i;
    }
    
    public void setGlobalTimeout(long l) {
        globalTimeout = l;
    }
    
    public void setModerate(Moderate moderate) {
        this.moderate = moderate;
    }
    
    public void setModList(CopyOnWriteArrayList<String> modList) {
        this.mods = modList;
    }
    
    public void setSubMessage(String subMessage) {
        this.subMessage = subMessage;
    }
    
    public void setQuotes(ArrayList<String> quotes) {
        this.quotes = quotes;
    }
    
    public void setRepeatList(RepeatList repeatList) {
        this.repeatList = repeatList;
    }
    
    public void setSubscriberList(CopyOnWriteArrayList<String> subscriberList) {
        this.subs = subscriberList;
    }
    
    public void setPoll(Poll poll) {
        this.poll = poll;
    }
    
    public void setCanSend(boolean canSend) {
        this.canSend = canSend;
    }
    
    public void clearModList() {
        mods.clear();
    }
    
    public void clearSubscriberList() {
        subs.clear();
    }
}
