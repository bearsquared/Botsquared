package botsquared;

import java.util.ArrayList;
import java.util.Random;

public class Channel {
    private String name;
    private CommandList list = new CommandList();
    private int globalDelay = 5;
    private transient long globalTimeout = 0L;
    private boolean moderate = false;
    private String subMessage = "(not set)";
    private ArrayList<String> quotes = new ArrayList<>();
    private RepeatList repeatList = new RepeatList();
    private transient ArrayList<String> modList = new ArrayList<>();
    private transient ArrayList<String> superList = new ArrayList<>();
    private transient ArrayList<String> permitList = new ArrayList<>();
    private transient Poll poll = new Poll();
    
    public Channel() {
        
    }
    
    public Channel(String name) {
        this.name = name;
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
    
    public boolean getModerate() {
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
    
    public ArrayList<String> getModList() {
        return modList;
    }
    
    public ArrayList<String> getSuperList() {
        return superList;
    }
    
    public ArrayList<String> getPermitList() {
        return permitList;
    }
    
    public Poll getPoll() {
        return poll;
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
    
    public void setModerate(boolean b) {
        moderate = b;
    }
    
    public void setModList(ArrayList<String> modList) {
        this.modList = modList;
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
    
    public void setSuperList(ArrayList<String> superList) {
        this.superList = superList;
    }
    
    public void setPermitList(ArrayList<String> permitList) {
        this.permitList = permitList;
    }
    
    public void setPoll(Poll poll) {
        this.poll = poll;
    }
    
    public void clearModList() {
        modList.clear();
    }
    
    public void clearSuperList() {
        superList.clear();
    }
}
