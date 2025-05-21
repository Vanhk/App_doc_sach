package com.example.appdocsachv2.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdocsachv2.R;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.view.activity.BookDetailActivity;
import com.example.appdocsachv2.view.activity.BookListActivity;
import com.example.appdocsachv2.view.activity.HomeActivity;

import java.util.ArrayList;
import java.util.List;

public class FavoriteBookAdapter extends RecyclerView.Adapter<FavoriteBookAdapter.FavoriteBookViewHolder> {
    private static final String TAG = "FavoriteBookAdapter";
    private List<Book> favoriteBookList;
    private HomeActivity homeActivity;
    private BookListActivity bookListActivity;
    private Context context;
    private int userId;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public FavoriteBookAdapter(List<Book> favoriteBookList, HomeActivity homeActivity, int userId, OnItemClickListener listener) {
        this.favoriteBookList = (favoriteBookList != null) ? favoriteBookList : new ArrayList<>();
        this.homeActivity = homeActivity;
        this.context = homeActivity;
        this.userId = userId;
        this.listener = listener;
    }

    public FavoriteBookAdapter(List<Book> favoriteBookList, BookListActivity bookListActivity, int userId, OnItemClickListener listener) {
        this.favoriteBookList = (favoriteBookList != null) ? favoriteBookList : new ArrayList<>();
        this.bookListActivity = bookListActivity;
        this.context = bookListActivity;
        this.userId = userId;
        this.listener = listener;
    }

    public void updateData(List<Book> newFavoriteBooks) {
        this.favoriteBookList = (newFavoriteBooks != null) ? newFavoriteBooks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new FavoriteBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteBookViewHolder holder, int position) {
        if (position < favoriteBookList.size()) {
            Book book = favoriteBookList.get(position);
            holder.bind(book);
        }
    }

    @Override
    public int getItemCount() {
        int count = favoriteBookList.size();
        return count;
    }

    class FavoriteBookViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgBookCover;
        private TextView txtBookTitle;
        private ImageView btnFavorite;

        public FavoriteBookViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBookCover = itemView.findViewById(R.id.imgBookCover);
            txtBookTitle = itemView.findViewById(R.id.txtBookTitle);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        public void bind(final Book book) {
            if (book == null) {
                return;
            }

            txtBookTitle.setText(book.getTitle() != null ? book.getTitle() : "Không có tiêu đề");
            if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
                Glide.with(context)
                        .load(book.getCoverImage())
                        .placeholder(R.drawable.noimage)
                        .error(R.drawable.noimage)
                        .into(imgBookCover);
            } else {
                imgBookCover.setImageResource(R.drawable.noimage);
            }

            View.OnClickListener openDetailListener = v -> {
                if (listener != null) {
                    listener.onItemClick(book);
                }
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra("book_id", book.getBookId());
                context.startActivity(intent);
            };
            imgBookCover.setOnClickListener(openDetailListener);
            txtBookTitle.setOnClickListener(openDetailListener);

            View btnEdit = itemView.findViewById(R.id.btnEdit);
            View btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);

            btnFavorite.setVisibility(View.VISIBLE);
            btnFavorite.setImageResource(R.drawable.baseline_bookmark_24);

            btnFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (homeActivity != null) {
                        homeActivity.removeFromFavorites(book.getBookId());
                        favoriteBookList.remove(position);
                        notifyItemRemoved(position);
                    } else if (bookListActivity != null) {
                        bookListActivity.removeFromFavorites(book.getBookId());
                        favoriteBookList.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            });
        }
    }
}