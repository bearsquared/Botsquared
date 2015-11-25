package botsquared;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;


public class Post {
    private static final String ENCODING = "UTF-8";
    private HashMap<String, String> post;
    
    public Post() {
        post = new HashMap<String, String>();
    }
    
    public void put(String key, String value) {
        try {
            this.post.put(URLEncoder.encode(key, ENCODING),
                    URLEncoder.encode(value, ENCODING));
        } catch (UnsupportedEncodingException e) {
        }
    }
    
    public String getPost() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : post.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
        }
        sb.deleteCharAt(sb.length() - 1);
        return new String(sb);
    }
}
