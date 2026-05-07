package com.hnreader;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.VH> {

    public interface OnStoryClick {
        void onOpen(Story story);
        void onComments(Story story);
    }

    private final List<Story> stories = new ArrayList<>();
    private OnStoryClick listener;

    public void setListener(OnStoryClick l) { this.listener = l; }

    public void setStories(List<Story> list) {
        stories.clear();
        stories.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_story, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Story s = stories.get(pos);

        h.rank.setText(String.valueOf(pos + 1));
        h.title.setText(s.title);
        h.domain.setText(s.getDomain());
        h.score.setText(s.score + " pts");
        h.meta.setText(s.by + "  ·  " + s.getTimeAgo());
        h.comments.setText(s.getCommentsLabel());

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onOpen(s); });
        h.comments.setOnClickListener(v -> { if (listener != null) listener.onComments(s); });
    }

    @Override public int getItemCount() { return stories.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView rank, title, domain, score, meta, comments;
        VH(View v) {
            super(v);
            rank     = v.findViewById(R.id.tvRank);
            title    = v.findViewById(R.id.tvTitle);
            domain   = v.findViewById(R.id.tvDomain);
            score    = v.findViewById(R.id.tvScore);
            meta     = v.findViewById(R.id.tvMeta);
            comments = v.findViewById(R.id.tvComments);
        }
    }
}
