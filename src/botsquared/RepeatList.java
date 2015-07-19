package botsquared;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RepeatList {
    /*private Map<String, Repeat> list = new HashMap<>();
    
    private static final String errorFail = "I encountered an error and was unable to <mode> your command.";
    private static final String exists = " The command <name> already exists.";
    
    public PairOld<Boolean, String> addRepeat(String parameters) {
        boolean errors = false;
        boolean added = false;
        String feedback = "";
        
        if (parameters.startsWith("!")) {
            String name = parameters.substring(0, parameters.indexOf(" "));
            parameters = parameters.replace(name, "").trim();
            
            if (list.get(name) == null) {
                int interval = 60;
                if (parameters.startsWith("\\u003c")) {
                    Pattern p = Pattern.compile("^\\u003c(.*?)\\u003e");
                    Matcher m = p.matcher(parameters);
                    
                    String value = "";
                    
                    if (m.find()) {
                        value = m.group(0);
                        parameters = parameters.replaceFirst(m.group(0), "").trim();
                    }
                    interval = parseInterval(value);
                }
            }
            else {
                feedback += errorFail + exists;
                feedback = feedback.replaceAll("<name>", name);
            }
        }
        
        PairOld pair = new PairOld(true, "");
        return pair;
    }
    
    private int parseInterval(String value) {
        int interval = 60;
        
        if (!value.isEmpty() || value.length() == 0) {
            Pattern p = Pattern.compile("\\u003c(interval)\\u003a\\w+\\u003e",
                            Pattern.CASE_INSENSITIVE |
                            Pattern.UNICODE_CASE );
            Matcher m = p.matcher(value);
            
            if (m.matches()) {
                String[] paramPair = value.replaceFirst("<", "").replaceFirst(">", "").split(":");
                
                try {
                    int i = Integer.parseInt(paramPair[1]);
                    if (i >= 60) {
                        interval = i;
                    }
                } catch (NumberFormatException e) {
                    //fields.add(field);
                }
            }
            
        }
        
        return interval;
    }
    
    public Map<String, Repeat> getList() {
        return list;
    }
    
    public void setList(Map<String, Repeat> list) {
        this.list = list;
    }*/
}
