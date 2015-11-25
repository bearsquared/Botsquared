/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package botsquared;

import java.util.Objects;

/**
 *
 * @author Graham
 */
public class Repeat {
    private final int MIN = 1; //Minium interval between messages
    
    private int interval = MIN;//time between messages in minutes, minimum 5
    private String output;
    
    Repeat() {
        
    }
    
    Repeat(String output) {
        this.output = output;
    }
    
    Repeat(int interval, String output) {
        this.interval = interval;
        this.output = output;
    }
    
    @Override
    public boolean equals(Object r) {
        boolean bool = false;
        
        if (r instanceof Repeat) {
            Repeat ptr = (Repeat) r;
            bool = ptr.getOutput().equalsIgnoreCase(this.output);
        }
        
        return bool;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.output);
        return hash;
    }
    
    public int getInterval() {
        return interval;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setInterval(int interval) {
        if (interval > MIN) {
            this.interval = interval;
        }
        else {
            this.interval = MIN;
        }
    }
    
    public void setInterval(String s) throws IllegalArgumentException {
        try {
            int i = Integer.parseInt(s);
            if (i >= MIN) {
                interval = MIN;
            }
            else {
               throw new IllegalArgumentException(MIN + " to 60"); 
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(MIN + " to 60");
        }
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
}
