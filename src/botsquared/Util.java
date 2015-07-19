/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package botsquared;

import java.util.regex.Pattern;

/**
 *
 * @author Graham
 */
public class Util {
    public static final String complexCom = "(^!\\w*)\\s+(<.*?>)\\s*(\\b.*)";
    public static final String simpCom = "(^!\\w+)\\s+(.+)";
    public static final String hasName = "^!\\w*";
    public static final String exactName = "^!\\w+$";
    public static final String aerP = "(^!\\w+)\\s+(add|edit|remove)\\s+(.+)";
    public static final String levelP = "(?:<level:)(MOD|OWNER|NATIVE|FUNCTIONAL|COMPLEX)>";
    public static final String accessP = "(?:<access:)(PUBLIC|SUB|MOD|OWNER|BEARSQUARED)>";
    public static final String globalP = "(?:<global:)(true|false)>";
    public static final String delayP = "(?:<delay:)([0-9]+)>";
    
    public static final Pattern pComplex = Pattern.compile(complexCom);
    public static final Pattern pSimple = Pattern.compile(simpCom);
    public static final Pattern pName = Pattern.compile(hasName);
    public static final Pattern pAER = Pattern.compile(aerP);
    public static final Pattern pLevel = Pattern.compile(levelP);
    public static final Pattern pAccess = Pattern.compile(accessP);
    public static final Pattern pGlobal = Pattern.compile(globalP);
    public static final Pattern pDelay = Pattern.compile(delayP);
}
