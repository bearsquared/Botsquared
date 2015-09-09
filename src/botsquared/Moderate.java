package botsquared;

import java.util.concurrent.CopyOnWriteArraySet;

public class Moderate {
   private boolean caps = false;
   private boolean links = false;
   private transient CopyOnWriteArraySet<String> permitList = new CopyOnWriteArraySet<>();
   
   public boolean getCaps() {
       return caps;
   }
   
   public void setCaps(boolean caps) {
       this.caps = caps;
   }
   
   public boolean getLinks() {
       return links;
   }
   
   public void setLinks(boolean links) {
       this.links = links;
   }
   
   public CopyOnWriteArraySet<String> getPermitList() {
       return permitList;
   }
   
   public void setPermitList(CopyOnWriteArraySet<String> permitList) {
       this.permitList = permitList;
   }
}
