package com.hnreader;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements StoryAdapter.OnStoryClick {

    private StoryAdapter       adapter;
    private SwipeRefreshLayout swipeRefresh;
    private TextView           tvError;
    private HnApi.Feed         currentFeed = HnApi.Feed.TOP;
    private ExecutorService    executor    = Executors.newSingleThreadExecutor();

    private final int[] TAB_IDS  = {R.id.tabTop, R.id.tabNew, R.id.tabAsk, R.id.tabShow};
    private final HnApi.Feed[] FEEDS = {HnApi.Feed.TOP, HnApi.Feed.NEW, HnApi.Feed.ASK, HnApi.Feed.SHOW};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // RecyclerView
        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StoryAdapter();
        adapter.setListener(this);
        rv.setAdapter(adapter);

        // Swipe refresh
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeColors(0xFFFF6600);
        swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF242424);
        swipeRefresh.setOnRefreshListener(() -> loadFeed(currentFeed));

        // Error view
        tvError = findViewById(R.id.tvError);

        // Tabs
        for (int i = 0; i < TAB_IDS.length; i++) {
            final HnApi.Feed feed = FEEDS[i];
            final int tabId = TAB_IDS[i];
            findViewById(tabId).setOnClickListener(v -> {
                currentFeed = feed;
                updateTabUI(tabId);
                loadFeed(feed);
            });
        }

        // Initial load
        updateTabUI(R.id.tabTop);
        loadFeed(HnApi.Feed.TOP);
    }

    private void loadFeed(HnApi.Feed feed) {
        swipeRefresh.setRefreshing(true);
        tvError.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
                int[] ids = HnApi.fetchIds(feed);
                java.util.List<Story> stories = HnApi.fetchStories(ids);
                runOnUiThread(() -> {
                    adapter.setStories(stories);
                    swipeRefresh.setRefreshing(false);
                    tvError.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("Couldn't load stories.\nCheck your connection and pull to retry.");
                });
            }
        });
    }

    private void updateTabUI(int activeTabId) {
        for (int id : TAB_IDS) {
            TextView tab = findViewById(id);
            if (id == activeTabId) {
                tab.setTextColor(0xFFFF6600);
                tab.setBackgroundResource(R.drawable.tab_active);
            } else {
                tab.setTextColor(0xFF888888);
                tab.setBackgroundResource(R.drawable.tab_inactive);
            }
        }
    }

    @Override
    public void onOpen(Story story) {
        String url = story.url.isEmpty()
            ? "https://news.ycombinator.com/item?id=" + story.id
            : story.url;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public void onComments(Story story) {
        String url = "https://news.ycombinator.com/item?id=" + story.id;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
