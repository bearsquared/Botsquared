package botsquared;

import java.util.concurrent.CopyOnWriteArraySet;


public class User implements Comparable<User> {
    private boolean staff = false, admin = false, global_mod = false, turbo = false;
    private String nick, lowerNick, displayName = null;
    
    private CopyOnWriteArraySet<Integer> emotes = new CopyOnWriteArraySet<>();
    
    public User(String nick) {
        this.nick = nick;
        this.lowerNick = nick.toLowerCase();
    }
    
    public String getPrefix() {
        StringBuilder prefix = new StringBuilder();
        
        if (isTurbo()) {
            prefix.append("+");
        }
        if (isGlobalMod()) {
            prefix.append("!");
        }
        if (isAdmin()) {
            prefix.append("!!");
        }
        if (isStaff()) {
            prefix.append("!!!");
        }
        return prefix.toString();
    }
    
    public boolean isOp(Channel c) {
        return c != null && c.isMod(getNick());
    }
    
    public boolean isGlobalMod() {
        return global_mod;
    }
    
    public void setGlobalMod(boolean bool) {
        global_mod = bool;
    }
    
    public boolean isAdmin() {
        return admin;
    }
    
    public void setAdmin(boolean bool) {
        admin = bool;
    }
    
    public boolean isStaff() {
        return staff;
    }
    
    public void setStaff(boolean bool) {
        staff = bool;
    }
    
    public boolean isTurbo() {
        return turbo;
    }
    
    public void setTurbo (boolean bool) {
        turbo = bool;
    }
    
    public boolean isSubscriber(Channel c) {
        return c != null && c.isSubscriber(this);
    }
    
    public void addEmote(int emote) {
        this.emotes.add(emote);
    }
    
    public CopyOnWriteArraySet<Integer> getEmotes() {
        return emotes;
    }
    
    public String getNick() {
        return nick;
    }
    
    public String getLowerNick() {
        return lowerNick;
    }
    
    public void setNick(String nick) {
        this.nick = nick;
        lowerNick = nick.toLowerCase();
    }
    
    public String getDisplayName() {
        return displayName == null ? getLowerNick() : displayName;
    }
    
    public void setDisplayName(String name) {
        if (displayName == null) {
            displayName = name;
        }
    }
    
    @Override
    public String toString() {
        return getPrefix() + getNick();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            User other = (User) o;
            return other.lowerNick.equals(lowerNick);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return lowerNick.hashCode();
    }
    
    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(User o) {
        if (o != null) {
            return o.lowerNick.compareTo(lowerNick);
        }
        return -1;
    }
}
