
package botsquared;

import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

public class OutputThread extends Thread {
    
    private BotSkeleton bot = null;
    private LinkedBlockingQueue<String> outQueue = new LinkedBlockingQueue<>();
    
    /**
     * Constructs an OutputThread for the underlying PircBot.  All messages
     * sent to the IRC server are sent by this OutputThread to avoid hammering
     * the server.  Messages are sent immediately if possible.  If there are
     * multiple messages queued, then there is a delay imposed.
     * 
     * @param bot The underlying PircBot instance.
     * @param outQueue The Queue from which we will obtain our messages.
     */
    OutputThread(BotSkeleton bot, LinkedBlockingQueue<String> outQueue) {
        this.bot = bot;
        this.outQueue = outQueue;
        this.setName(this.getClass() + "-Thread");
    }
    
    /**
     * A static method to write a line to a BufferedOutputStream and then pass
     * the line to the log method of the supplied PircBot instance.
     * 
     * @param bot The underlying PircBot instance.
     * @param out The BufferedOutputStream to write to.
     * @param line The line to be written. "\r\n" is appended to the end.
     * @param encoding The charset to use when encoding this string into a
     *                 byte array.
     */
    static void sendRawLine(BotSkeleton bot, BufferedWriter bWriter, String line) {
        if (line.length() > bot.getMaxLineLength() - 2) {
            line = line.substring(0, bot.getMaxLineLength() - 2);
        }
        synchronized(bWriter) {
            try {
                bWriter.write(line + "\r\n");
                bWriter.flush();
                bot.log(">>>" + line);
            } catch (Exception e) {
                // Silent response - just lose the line.
            }
        }
    }
    
    /**
     * This method starts the Thread consuming from the outgoing message
     * Queue and sending lines to the server.
     */
    @Override
    public void run() {
        try {
            boolean running = true;
            while (running) {
                // Small delay to prevent spamming of the channel
                Thread.sleep(bot.getMessageDelay());
                
                String line = outQueue.take();
                if (line != null) {
                    bot.sendRawLine(line);
                }
                else {
                    running = false;
                }
            }
        } catch (InterruptedException e) {
            // Just let the method return naturally...
        }
    }
}
