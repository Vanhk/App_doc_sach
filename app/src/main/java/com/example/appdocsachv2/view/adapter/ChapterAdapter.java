package com.example.appdocsachv2.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdocsachv2.R;
import com.example.appdocsachv2.model.Chapter;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {
    private List<Chapter> chapterList;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(Chapter chapter);
    }

    public ChapterAdapter(List<Chapter> chapterList, OnItemClickListener listener) {
        this.chapterList = chapterList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);
        holder.bind(chapter, listener, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        private TextView txtChapterTitle;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChapterTitle = itemView.findViewById(R.id.txtChapterTitle);
        }

        public void bind(Chapter chapter, OnItemClickListener listener, boolean isSelected) {
            txtChapterTitle.setText(chapter.getTitle());
            if (isSelected) {
                txtChapterTitle.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.selected_background));
            } else {
                txtChapterTitle.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
            }
            itemView.setOnClickListener(v -> listener.onItemClick(chapter));
        }
    }
}