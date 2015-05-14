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
public class PollOption {
    private String name;
    private int votes;
    
    public PollOption() {
        
    }
    
    public PollOption(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public int getVotes() {
        return votes;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setVotes(int votes) {
        this.votes = votes;
    }
    
    public void addVote() {
        votes++;
    }
}
