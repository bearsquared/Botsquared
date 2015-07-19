/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package botsquared;

/**
 *
 * @author Graham
 */
public class Repeat {
    private String name;
    private int interval = 60;//time between messages in seconds, minimum 60
    private String output;
    private transient int msl = 0; //Messages Since Last: keeps track of how many messages been sent since this the last message
    
    public String getName() {
        return name;
    }
    
    public int getInterval() {
        return interval;
    }
    
    public String getOutput() {
        return output;
    }
    
    public int getMsl() {
        return msl;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setInterval(int interval) {
        if (interval > 60) {
            this.interval = interval;
        }
        else {
            this.interval = 60;
        }
    }
    
    public void setInterval(String s) throws IllegalArgumentException {
        try {
            int i = Integer.parseInt(s);
            if (i >= 60) {
                interval = i;
            }
            else {
               throw new IllegalArgumentException("0 to 3600"); 
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("0 to 3600");
        }
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
    public void setMsl(int msl) {
        this.msl = msl;
    }
}
