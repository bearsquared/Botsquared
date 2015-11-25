/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package botsquared;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.javatuples.Tuple;
import org.javatuples.Unit;
import org.jsoup.Jsoup;

/**
 *
 * @author Graham
 */
public class Util {
    public static final String url = "(?:((?:https?|ftp|file):\\/\\/www\\.)|((?:https?|ftp|file):\\/\\/(?!www.))|(www\\.))[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%?=~_|]";
    public static final String excCaps = "((\\b[A-Z]{2,40}\\b)\\s*){10,}";
    
    public static final String command = "(^![a-zA-Z0-9_]{3,})\\s+(<.*>)?\\s+((?![<>]).+)";
    public static final String complexCom = "(^![a-zA-Z0-9_]{3,})\\s+(<.*>)\\s+((?![<>]).+)";
    public static final String simpCom = "(^![a-zA-Z0-9_]{3,})\\s+((?![<>]).+)";
    public static final String hasName = "(^![a-zA-Z0-9_]{3,})[\\s\\S]*?";
    public static final String hasNameParams = "(^![a-zA-Z0-9_]{3,})\\s+(<.*>)$";
    public static final String exactName = "(^![a-zA-Z0-9_]{3,}$)";
    public static final String aerP = "(^![a-zA-Z0-9_]{3,})\\s+(add|edit|remove)\\s+(.+)";
    public static final String levelP = "(?:<level:)(MOD|OWNER|NATIVE|COMPLEX)>";
    public static final String accessP = "(?:<access:)(PUBLIC|SUB|MOD|OWNER|BEARSQUARED)>";
    public static final String globalP = "(?:<global:)(true|false)>";
    public static final String delayP = "(?:<delay:)([0-9]+)>";
    
    public static final String repeat = "^(<.*>)?(?:\\s+)?((?![<>]).+)";
    public static final String complexRepeat = "^(<.*>)\\s+((?![<>]).+)";
    public static final String simpleRepeat = "^((?![<>]).+)";
    
    public static final Pattern pURL = Pattern.compile(url);
    public static final Pattern pExcCaps = Pattern.compile(excCaps);
    
    public static final Pattern pCommand = Pattern.compile(command);
    public static final Pattern pComplex = Pattern.compile(complexCom);
    public static final Pattern pSimple = Pattern.compile(simpCom);
    public static final Pattern pExactName = Pattern.compile(exactName);
    public static final Pattern pName = Pattern.compile(hasName);
    public static final Pattern pNameParams = Pattern.compile(hasNameParams);
    public static final Pattern pAER = Pattern.compile(aerP, Pattern.CASE_INSENSITIVE);
    public static final Pattern pLevel = Pattern.compile(levelP, Pattern.CASE_INSENSITIVE);
    public static final Pattern pAccess = Pattern.compile(accessP, Pattern.CASE_INSENSITIVE);
    public static final Pattern pGlobal = Pattern.compile(globalP, Pattern.CASE_INSENSITIVE);
    public static final Pattern pDelay = Pattern.compile(delayP);
    
    public static final Pattern pRepeat = Pattern.compile(repeat);
    public static final Pattern pComplexRepeat = Pattern.compile(complexRepeat);
    public static final Pattern pSimpleRepeat = Pattern.compile(simpleRepeat);
        
    /**
     * Checks if a message contains a URL
     * 
     * @param message
     * @return 
     */
    public static boolean isLink(String message) {
        Matcher matcher = pURL.matcher(message);
        
        return matcher.find();
    }
    
    public static boolean isExcCaps(String message) {
        Matcher matcher = pExcCaps.matcher(message);
        return matcher.matches();
    }
    
    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
    
    public static boolean isOnline(String channel) {
        String html = "https://api.twitch.tv/kraken/streams/" + channel.replaceFirst("#", "");
        
        try {
            String json = Jsoup.connect(html).ignoreContentType(true).execute().body();
            Gson gson = new Gson();
            StreamAPI stream = gson.fromJson(json, StreamAPI.class);
            return stream.getStream() != null;
        } catch (IOException ex) {
            Logger.getLogger(Botsquared.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public static StreamAPI getUptime(String channel) {
        String html = "https://api.twitch.tv/kraken/streams/" + channel.replaceFirst("#", "");
        
        try {
            String json = Jsoup.connect(html).ignoreContentType(true).execute().body();
            Gson gson = new Gson();
            StreamAPI stream = gson.fromJson(json, StreamAPI.class);
            return stream;
        } catch (IOException ex) {
            Logger.getLogger(Botsquared.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
        
    public static Tuple splitAERMessage(String message) {
        Matcher mAER = pAER.matcher(message);
        Matcher mComplex = pComplex.matcher(message);
        Matcher mNameParams = pNameParams.matcher(message);
        Matcher mSimple = pSimple.matcher(message);
        Matcher mName = pName.matcher(message);

        if (mAER.matches()) {
            return new Triplet<>(mAER.group(1),mAER.group(2),mAER.group(3));
        }
        else if (mComplex.matches()) {
            return new Triplet<>(mComplex.group(1),mComplex.group(2),mComplex.group(3));
        }
        else if (mNameParams.matches()) {
            return new Pair<>(mNameParams.group(1), mNameParams.group(2));
        }
        else if (mSimple.matches()) {
            return new Pair<>(mSimple.group(1),mSimple.group(2));
        }
        else if (mName.matches()) {
            return new Unit<>(mName.group(1));
        }
        else {
            return new Unit<>("ERROR");
        }
    }
    
    public static Tuple splitRepeatMessage(String message) {
        Matcher mComplexRepeat = pComplexRepeat.matcher(message);
        Matcher mSimpleRepeat = pSimpleRepeat.matcher(message);
        
        if (mComplexRepeat.matches()) {
            return new Pair<>(mComplexRepeat.group(2), mComplexRepeat.group(1));
        }
        else if (mSimpleRepeat.matches()) {
            System.out.println("GOT THERE: " + mSimpleRepeat.group(1));
            return new Unit<>(mSimpleRepeat.group(1));
        }
        else {
            System.out.println("THERE WAS AN ERROR");
            return new Unit<>("ERROR");
        }
    }
}
