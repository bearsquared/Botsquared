package botsquared;

public class Pastebin {
    /*
    * URL for Pastebin API
    */
    public static final String API_POST = "http://pastebin.com/api/api_post.php";
    
    public static final String devKey = "316319288e790c87e8fd49ff1605159b";
    
    public static final String PUBLIC = "0";
    
    public static String generate(String channel, String contents) {
        Post post = new Post();
        
        post.put("api_dev_key", devKey);
        post.put("api_option", "paste");
        post.put("api_paste_code", contents);
        post.put("api_paste_name", channel);
        post.put("api_paste_private", PUBLIC);
        
        String pageResponse = Web.getContents(Pastebin.API_POST, post);
        if (pageResponse.startsWith("http")) {
            // success
            return pageResponse;
        }
        else {
            return "ERROR";
        }
    }
}
