package botsquared;

import java.util.ArrayList;
import java.util.Timer;


public class RepeatList {
    private ArrayList<Repeat> list = new ArrayList<>();
    
    /* 
       When a timer expires, check number of messages sent between the last repeating messages. 
       If enough have been sent, send the repeating message. If not, add it to the queue. 
       Every time a message is sent, check again. When enough messages have been sent, dequeue.
    */
    
    
    public void startTimers() {
        for (int i = 0; i < list.size(); i++) {
            Timer timer = new Timer();
        }
    }
    
    public ArrayList<Repeat> getList() {
        return list;
    }
    
    public void setList(ArrayList<Repeat> list) {
        this.list = list;
    }
}
