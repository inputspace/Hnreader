package com.hnreader;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class HnApi {

    private static final String BASE    = "https://hacker-news.firebaseio.com/v0";
    private static final int    TIMEOUT = 10000;
    private static final int    STORIES = 30;

    public enum Feed { TOP, NEW, ASK, SHOW }

    public static int[] fetchIds(Feed feed) throws Exception {
        String path;
        switch (feed) {
            case NEW:  path = "/newstories.json";  break;
            case ASK:  path = "/askstories.json";  break;
            case SHOW: path = "/showstories.json"; break;
            default:   path = "/topstories.json";  break;
        }
        String json = get(BASE + path);
        JSONArray arr = new JSONArray(json);
        int count = Math.min(arr.length(), STORIES);
        int[] ids = new int[count];
        for (int i = 0; i < count; i++) ids[i] = arr.getInt(i);
        return ids;
    }

    public static List<Story> fetchStories(int[] ids) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(6);
        List<Future<Story>> futures = new ArrayList<>();

        for (int id : ids) {
            final int storyId = id;
            futures.add(pool.submit(() -> {
                try {
                    String json = get(BASE + "/item/" + storyId + ".json");
                    if (json == null || json.equals("null")) return null;
                    return Story.fromJson(new JSONObject(json));
                } catch (Exception e) {
                    return null;
                }
            }));
        }

        pool.shutdown();
        pool.awaitTermination(15, TimeUnit.SECONDS);

        List<Story> stories = new ArrayList<>();
        for (Future<Story> f : futures) {
            try {
                Story s = f.get();
                if (s != null && s.title != null && !s.title.isEmpty()) {
                    stories.add(s);
                }
            } catch (Exception ignored) {}
        }
        return stories;
    }

    private static String get(String urlStr) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != 200) return null;
            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
