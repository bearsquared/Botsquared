/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package botsquared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.javatuples.Tuple;
import org.javatuples.Unit;

/**
 *
 * @author Graham
 */
public class Util {
    public static final String command = "(^!\\w+)\\s+(<.*?>)?\\s*(\\b.*)";
    public static final String complexCom = "(^!\\w+)\\s+(<.*?>)\\s*(\\b.*)";
    public static final String simpCom = "(^!\\w+)\\s+(.+)";
    public static final String hasName = "(^!\\w+)[\\s\\S]*?";
    public static final String exactName = "(^!\\w+$)";
    public static final String aerP = "(^!\\w+)\\s+(add|edit|remove)\\s+(.+)";
    public static final String levelP = "(?:<level:)(MOD|OWNER|NATIVE|FUNCTIONAL|COMPLEX)>";
    public static final String accessP = "(?:<access:)(PUBLIC|SUB|MOD|OWNER|BEARSQUARED)>";
    public static final String globalP = "(?:<global:)(true|false)>";
    public static final String delayP = "(?:<delay:)([0-9]+)>";
    
    public static final Pattern pCommand = Pattern.compile(command);
    public static final Pattern pComplex = Pattern.compile(complexCom);
    public static final Pattern pSimple = Pattern.compile(simpCom);
    public static final Pattern pExactName = Pattern.compile(exactName);
    public static final Pattern pName = Pattern.compile(hasName);
    public static final Pattern pAER = Pattern.compile(aerP, Pattern.CASE_INSENSITIVE);
    public static final Pattern pLevel = Pattern.compile(levelP, Pattern.CASE_INSENSITIVE);
    public static final Pattern pAccess = Pattern.compile(accessP, Pattern.CASE_INSENSITIVE);
    public static final Pattern pGlobal = Pattern.compile(globalP, Pattern.CASE_INSENSITIVE);
    public static final Pattern pDelay = Pattern.compile(delayP);
        
    public static Tuple splitMessage(String message) {
        Matcher mAER = pAER.matcher(message);
        Matcher mComplex = pComplex.matcher(message);
        Matcher mSimple = pSimple.matcher(message);
        Matcher mName = pName.matcher(message);

        if (mAER.matches()) {
            return new Triplet<>(mAER.group(1),mAER.group(2),mAER.group(3));
        }
        else if (mComplex.matches()) {
            return new Triplet<>(mComplex.group(1),mComplex.group(2),mComplex.group(3));
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
}
