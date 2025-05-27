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
    private int selectedPosition = -1;//không có chương nào đc chọn

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
//Lấy đối tượng Chapter tại vị trí position từ chapterList
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
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition); // Cập nhật item cũ
        }
        selectedPosition = position;
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition); // Cập nhật item mới
        }
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
                itemView.setBackgroundResource(R.drawable.selected_item_background);
                txtChapterTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                txtChapterTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(chapter);
                }
            });
        }
    }
}