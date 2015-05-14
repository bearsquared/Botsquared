/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package botsquared;

import java.util.ArrayList;

/**
 *
 * @author Graham
 */
public class Poll {
    boolean open = false;
    ArrayList<PollOption> options = new ArrayList<>();
    ArrayList<String> voted = new ArrayList<>();
    
    public boolean getOpen() {
        return open;
    }
    
    public ArrayList<PollOption> getOptions() {
        return options;
    }
    
    public ArrayList<String> getVoted() {
        return voted;
    }
    
    public void setOpen(boolean open) {
        this.open = open;
    }
    
    public void setOptions(ArrayList<PollOption> options) {
        this.options = options;
    }
    
    public void setVoted(ArrayList<String> voted) {
        this.voted = voted;
    }
    
    public String open(ArrayList<PollOption> options) {
        if (open) {
            return "There is already a poll open with the following options:" + optionsToString() + ". To open a new poll, please close the existing poll.";
        }
        else {
            setOpen(true);
            setOptions(options);
            return "A poll has been opened with the following options:" + optionsToString() + ". Type \"!vote [Option Number]\" to cast your vote.";
        }
    }
    
    public void vote(String sender, int optionNumber) throws NullPointerException {
        if (!voted.contains(sender)) {
            voted.add(sender);
            options.get(optionNumber - 1).addVote();
        }
    }
    
    public String close() {
        if (open) {
            String tally = results();
            setOpen(false);
            
            voted.clear();
            
            
            return "The poll has been closed. " + tally;
        }
        
        else {
            return "There isn't an open poll. If you want to open a new poll, use \"!poll open <option 1> | <option 2> | ...\" instead.";
        }
    }
    
    public String reset() {
        if (open) {
            options.stream().forEach((o) -> {
                o.setVotes(0);
            });
            
            voted.clear();

            return "The poll has been reset with the following options:" + optionsToString() + ". Type \"!vote [Option Number]\" to cast your vote.";
        }
        
        else {
            return "There isn't an open poll. If you want to open a new poll, use \"!poll open <option 1> | <option 2> | ...\" instead.";
        }
    }
    
    public String results() {
        if (open) {
            String result = "Results: ";

            for(PollOption o : options) {
                result += o.getName() + " - " + o.getVotes() + " " + "votes";
                
                if (options.indexOf(o) != options.size() - 1) {
                    result += ", ";
                }
            }

            return result + ".";
        }
        
        else {
            return "There isn't an open poll. If you want to open a new poll, use \"!poll open <option 1> | <option 2> | ...\" instead.";
        }
    }
    
    private String optionsToString() {
        String s = "";
        
        for(PollOption o : options) {
            s += " " + "(#" + (options.indexOf(o) + 1) + ") " + o.getName();
            
            if (options.indexOf(o) != options.size() - 1) {
                s+= ",";
            }
        }
        
        return s;
    }
}
