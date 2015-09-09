/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package botsquared;

import java.io.*;
import java.net.*;
import java.util.*;


public class InputThread extends Thread {
    
    private BotSkeleton bot = null;
    private Socket socket = null;
    private BufferedReader bReader = null;
    private BufferedWriter bWriter = null;
    private boolean isConnected = true;
    private boolean disposed = false;
    
    public static final int MAX_LINE_LENGTH = 512;
    
    /**
     * The InputThread reads lines from the IRC server and allows the
     * PircBot to handle them.
     *
     * @param bot An instance of the underlying PircBot.
     * @param breader The BufferedReader that reads lines from the server.
     * @param bwriter The BufferedWriter that sends lines to the server.
     */
    InputThread(BotSkeleton bot, Socket socket, BufferedReader bReader, BufferedWriter bWriter) {
        this.bot = bot;
        this.socket = socket;
        this.bReader = bReader;
        this.bWriter = bWriter;
        this.setName(this.getClass() + "-Thread");
    }
    
    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     *
     * @param line The raw line to send to the IRC server.
     */
    void sendRawLine(String line) {
        OutputThread.sendRawLine(bot, bWriter, line);
    }
    
     /**
     * Returns true if this InputThread is connected to an IRC server.
     * The result of this method should only act as a rough guide,
     * as the result may not be valid by the time you act upon it.
     * 
     * @return True if still connected.
     */
    boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Called to start this Thread reading lines from the IRC server.
     * When a line is read, this method calls the handleLine method
     * in the PircBot, which may subsequently call an 'onXxx' method
     * in the PircBot subclass.  If any subclass of Throwable (i.e.
     * any Exception or Error) is thrown by your method, then this
     * method will print the stack trace to the standard output.  It
     * is probable that the PircBot may still be functioning normally
     * after such a problem, but the existance of any uncaught exceptions
     * in your code is something you should really fix.
     */
    @Override
    public void run() {
        try {
            boolean running = true;
            while (running) {
                try {
                    String line = null;
                    while ((line = bReader.readLine()) != null) {
                        try {
                            bot.handleLine(line);
                        } catch (Throwable t) {
                            //Stick the whole stack trace into a String so we can output it nicely.
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            
                            t.printStackTrace(pw);
                            pw.flush();
                            
                            StringTokenizer tokenizer = new StringTokenizer(sw.toString(), "\r\n");
                            
                            synchronized (bot) {
                                bot.log("### An uncaught Exception or Error has progagated in your");
                                bot.log("### code. It may be possible for the Bot to continue operating");
                                bot.log("### normally. Here is the stack trace that was produced: -");
                                bot.log("### ");
                                while (tokenizer.hasMoreTokens()) {
                                    bot.log("### " + tokenizer.nextToken());
                                }
                            }
                        }
                    }
                    if (line == null) {
                        // The server must have disconnected us.
                        running = false;
                    }
                } catch (InterruptedIOException iioe) {
                    // This will happen if we haven't received anything from the server for a while.
                    // So we shall send it a ping to check that we are still connected.
                    this.sendRawLine("PING " + (System.currentTimeMillis() / 1000));
                    // Now we go back to listening for stuff from the server...
                }
            }
        } catch (Exception e) {
            // Do nothing.
        }
        
        //If we reach this point, then we must have disconnected.
        try {
            socket.close();
        } catch (Exception e) {
            // Just assume the socket was already closed.
        }
        
        if (!disposed) {
            bot.log("*** Disconnected.");
            isConnected = false;
            bot.onDisconnect();
        }
    }
    
    public void dispose() {
        try {
            disposed = true;
            socket.close();
        } catch (Exception e) {
            // Do nothing.
        }
    }
}
