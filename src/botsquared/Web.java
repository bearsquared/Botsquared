/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package botsquared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Graham
 */
public class Web {
    /**
	 * Submits a HTTP post and fetches and returns the response
	 * 
	 * @param link
	 *            The link/URL
	 * @param post
	 *            the HTTP post representation
	 * @return response of the web page
	 */
	public static String getContents(String link, Post post) {
		try {
			URL url = new URL(link);

			URLConnection connection = url.openConnection();

			if(post != null) {
				connection.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(
						connection.getOutputStream());
				wr.write(post.getPost());
				wr.flush();
				wr.close();
			}

                        StringBuilder builder;
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                                connection.getInputStream()))) {
                            builder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (builder.length() > 0) {
                                    builder.append('\n');
				}
				builder.append(line);
			}
                    }
			return new String(builder);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Malformed link: " + e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to fetch contents from link: "
					+ e);
		}
	}
	
	/**
	 * Gets text from a link
	 * 
	 * @param link
	 *            The link/URL
	 * @return response of the web page
	 */
	public static String getContents(String link) {
		return getContents(link, null);
	}
}
