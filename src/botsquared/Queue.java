
package botsquared;

import java.util.Vector;

public class Queue {
    private Vector queue = new Vector();
    
    /**
     * Constructs a Queue object of unlimited size.
     */
    public Queue() {}
    
    /**
     * Adds an Object to the end of the Queue.
     *
     * @param o The Object to be added to the Queue.
     */
    public void add(Object o) {
        synchronized(queue) {
            queue.addElement(o);
            queue.notify();
        }
    }
    
    /**
     * Adds an Object to the front of the Queue.
     * 
     * @param o The Object to be added to the Queue.
     */
    public void addFront(Object o) {
        
        synchronized(queue) {
            queue.insertElementAt(o, 0);
            queue.notify();
        }
    }
    
    /**
     * Returns the Object at the front of the Queue.  This
     * Object is then removed from the Queue.  If the Queue
     * is empty, then this method shall block until there
     * is an Object in the Queue to return.
     *
     * @return The next item from the front of the queue.
     */
    public Object next() {
        
        Object o = null;
        
        // Block if the Queue is empty.
        synchronized(queue) {
            if (queue.size() == 0) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    return null;
                }
            }
            
            // Return the Object
            try {
                o = queue.firstElement();
                queue.removeElementAt(0);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new InternalError("Race hazard in Queue object.");
            }
        }
        
        return o;
    }
    
    /**
     * Returns true if the Queue is not empty.  If another
     * Thread empties the Queue before <b>next()</b> is
     * called, then the call to <b>next()</b> shall block
     * until the Queue has been populated again.
     *
     * @return True only if the Queue not empty.
     */
    public boolean hasNext() {
        return (this.size() != 0);
    }
    
    /**
     * Clears the contents of the Queue.
     */
    public void clear() {
        synchronized(queue) {
            queue.removeAllElements();
        }
    }
    
    /**
     * Returns the size of the Queue.
     *
     * @return The current size of the queue.
     */
    public int size() {
        return queue.size();
    }
}
