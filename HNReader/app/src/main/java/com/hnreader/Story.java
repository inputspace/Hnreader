package com.hnreader;

import org.json.JSONObject;
import java.util.Locale;

public class Story {
    public int    id;
    public String title;
    public String url;
    public String by;
    public int    score;
    public int    descendants;
    public long   time;
    public String type;

    public static Story fromJson(JSONObject j) {
        Story s = new Story();
        s.id          = j.optInt("id");
        s.title       = j.optString("title", "(no title)");
        s.url         = j.optString("url", "");
        s.by          = j.optString("by", "unknown");
        s.score       = j.optInt("score", 0);
        s.descendants = j.optInt("descendants", 0);
        s.time        = j.optLong("time", 0);
        s.type        = j.optString("type", "story");
        return s;
    }

    public String getDomain() {
        if (url == null || url.isEmpty()) return "news.ycombinator.com";
        try {
            String h = url.replaceFirst("https?://(www\\.)?", "");
            int slash = h.indexOf('/');
            return slash > 0 ? h.substring(0, slash) : h;
        } catch (Exception e) {
            return "";
        }
    }

    public String getTimeAgo() {
        long diff = System.currentTimeMillis() / 1000 - time;
        if (diff < 60)   return diff + "s ago";
        if (diff < 3600) return (diff / 60) + "m ago";
        if (diff < 86400) return (diff / 3600) + "h ago";
        return (diff / 86400) + "d ago";
    }

    public String getCommentsLabel() {
        return descendants == 0 ? "no comments"
             : descendants == 1 ? "1 comment"
             : descendants + " comments";
    }
}
