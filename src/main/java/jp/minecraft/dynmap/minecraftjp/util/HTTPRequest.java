package jp.minecraft.dynmap.minecraftjp.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: ayu
 * Date: 13/06/08
 * Time: 9:03
 * To change this template use File | Settings | File Templates.
 */
public class HTTPRequest {
    public final static String GET = "GET";
    public final static String POST = "POST";
    public final static String PUT = "PUT";
    public final static String DELETE = "DELETE";
    public final static String PATCH = "PATCH";

    private final static String USER_AGENT = "DynmapMinecraftJP/1.0";
    private String method;
    private String url;
    private final HashMap<String, String> postParameter = new HashMap<>();
    private final HashMap<String, String> headers = new HashMap<>();

    private HTTPRequest(String method, String url) {
        this.method = method;
        this.url = url;
        addHeader("User-Agent", USER_AGENT);
    }

    public void addPostParameter(String key, String value) {
        postParameter.put(key, value);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String send() throws IOException {
        URL urlObj = new URL(url);
        URLConnection conn = urlObj.openConnection();
        for (String key: headers.keySet()) {
            conn.setRequestProperty(key, headers.get(key));
        }


        if (method.equals(POST)) {
            conn.setDoOutput(true);
            StringBuilder sb = new StringBuilder();
            for (String key : postParameter.keySet()) {
                if (sb.length() != 0) sb.append("&");

                sb.append(key).append("=").append(postParameter.get(key));
            }

            OutputStream os = conn.getOutputStream();
            PrintStream ps = new PrintStream(os);
            ps.print(sb.toString());
            ps.close();
        }

        StringBuilder bodySb = new StringBuilder();
        InputStream is = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            bodySb.append(line);
        }
        br.close();

        return bodySb.toString();
    }

    public static HTTPRequest get(String url) {
        return new HTTPRequest(GET, url);
    }

    public static HTTPRequest post(String url) {
        return new HTTPRequest(POST, url);
    }
}
