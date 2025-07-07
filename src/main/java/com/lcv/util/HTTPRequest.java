package com.lcv.util;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class HTTPRequest
{
    private static final Logger log = LoggerFactory.getLogger(HTTPRequest.class);
    public static HashMap<String, HttpRequestCache> cache = new HashMap<>();

    public static void purgeCache() {
        cache.clear();
    }

    public static boolean purgeCache(String URL) {
        return cache.remove(URL) != null;
    }

    public static JSONObject getHTTPRequest(String URL)
    {
        if (cache.containsKey(URL)) {
            HttpRequestCache cached = cache.get(URL);
            if (!cached.tryPurge()) {
                return cached.result;
            }
        }


        try
        {
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode / 100 == 2)
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line);
                }
                reader.close();
                connection.disconnect();

                JSONObject js = new JSONObject(stringBuilder.toString());
                cache.put(URL, new HttpRequestCache(URL, js));
                return js;
            }
            return null;
        }
        catch(IOException e)
        {
            log.error("e: ", e);
            return null;
        }
    }

    public static class HttpRequestCache {
        static long cacheTime = 180;

        public final JSONObject result;

        public final String url;

        public final long expireAt;


        public HttpRequestCache(String URL, JSONObject result) {
            this.url = URL;
            this.result = result;
            expireAt = System.currentTimeMillis() + cacheTime*1000;
        }

        public boolean tryPurge() {
            if (System.currentTimeMillis() < this.expireAt) {
                return false;
            }

            cache.remove(this.url);
            return true;
        }
    }
}
