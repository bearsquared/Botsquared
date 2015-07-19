package botsquared;

/**
 *
 * @param <K>
 * @param <V>
 */
public class PairOld<K, V> {
    private K key;
    private V value;
    
    public PairOld(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    public K getKey() {
        return key;
    }
    
    public V getValue() {
        return value;
    }
}
