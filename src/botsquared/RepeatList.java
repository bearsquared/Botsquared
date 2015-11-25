package botsquared;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.javatuples.*;


public class RepeatList {
    private transient BotSkeleton bot = null;
    private String channel = null;
    private transient Channel thisChannel = new Channel();
    private ArrayList<Repeat> list = new ArrayList<>();
    private transient Queue<Repeat> queue = new ConcurrentLinkedQueue<>();
    
    private static final String errorFail = "I encountered an error and was unable to <mode> your repeating message.";
    private static final String exists = " A repeating message with that output already exists.";
    private static final String doesntExist = " The repeating message #<id> does not exist.";
    private static final String noPermission = " You do not have permission to <mode> this repeating message.";
    private static final String noOutput = " I was unable to find a valid output.";
    private static final String success = "Your repeating message has been <mode>ed successfully <errors> errors.";
    
    public void startAllTimers() {
        int lastDelay = 5;
        
        for (Repeat r : list) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (thisChannel.getCanSend() && Util.isOnline(channel)) {
                        bot.sendMessage(channel, "#" + (list.indexOf(r) + 1) + ": " + r.getOutput());
                        thisChannel.setCanSend(false);
                        startTimer(r);
                    }
                    else {
                        queue.add(r);
                    }
                }
            }, 60 * 1000 * (lastDelay + r.getInterval()));
            lastDelay += r.getInterval();
        }
    }
    
    public void startTimer(Repeat r) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (thisChannel.getCanSend() && Util.isOnline(channel)) {
                    bot.sendMessage(channel, "#" + (list.indexOf(r) + 1) + ": " + r.getOutput());
                    thisChannel.setCanSend(false);
                    startTimer(r);
                }
                else {
                    queue.add(r);
                }
            }
        }, 60 * 1000 * r.getInterval());
    }
    
    public synchronized void consume() {
        if (Util.isOnline(channel)) {
            Repeat r = queue.poll();

            if (r != null) {
                bot.sendMessage(channel, "#" + (list.indexOf(r) + 1) + ": " + r.getOutput());
                thisChannel.setCanSend(false);
                startTimer(r);
            }
        }
    }
    
    public Pair<Boolean, String> add(String message) {
        boolean errors = false;
        boolean added = false;
        String feedback = "";
        
        Tuple string = Util.splitRepeatMessage(message);
        
        if (!list.contains(new Repeat(string.getValue(0).toString()))) {
            if (string.getSize() == 2) {
                added = true;
                if (Util.isInteger(string.getValue(1).toString())) {
                    Repeat r = new Repeat(Integer.parseInt(string.getValue(1).toString()), string.getValue(0).toString());
                    list.add(r);
                    startTimer(r);
                }
                else {
                    Repeat r = new Repeat(string.getValue(0).toString());
                    list.add(r);
                    startTimer(r);
                }
            }
            else if (string.getSize() == 1) {
                if (!string.getValue(0).toString().equalsIgnoreCase("ERROR")) {
                    added = true;
                    Repeat r = new Repeat(string.getValue(0).toString());
                    list.add(r);
                    startTimer(r);
                }
                else {
                    errors = true;
                    feedback += errorFail + noOutput;
                }
            }
        }
        else {
            errors = true;
            feedback += errorFail + exists;
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
    
    public Pair<Boolean, String> remove(String message) {
        boolean errors = false;
        boolean removed = false;
        String feedback = "";
        
        if (Util.isInteger(message)) {
            int idx = Integer.parseInt(message) - 1;
            if (list.get(idx) != null) {
                removed = true;
                list.remove(idx);
                feedback += "Your repeating message has been removed successfully.";
            }
            else {
                errors = true;
                feedback += errorFail + doesntExist;
            }
        }
        else {
            errors = true;
            feedback += errorFail + "I was not able to find a valid ID.";
        }
        
        feedback = feedback.replaceAll("<mode>", "remove");
        
        Pair<Boolean, String> result = new Pair<>(removed, feedback);
        
        return result;
    }
    
    public BotSkeleton getBot() {
        return bot;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public ArrayList<Repeat> getList() {
        return list;
    }
    
    public Queue getQueue() {
        return queue;
    }
    
    public void setBot(BotSkeleton bot) {
        this.bot = bot;
        if (channel != null) {
            thisChannel = bot.getChannelHandler().getChannel(channel);
        }
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public void setList(ArrayList<Repeat> list) {
        this.list = list;
    }
    
    public void setQueue(Queue<Repeat> queue) {
        this.queue = queue;
    }
}
