package com.example.appdocsachv2.view.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.BookController;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;
import com.example.appdocsachv2.view.activity.AddEditBookActivity;
import com.example.appdocsachv2.view.activity.BookDetailActivity;
import com.example.appdocsachv2.view.activity.BookListActivity;
import com.example.appdocsachv2.view.activity.HomeActivity;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private static final String TAG = "BookAdapter";
    private List<Book> bookList;
    private OnItemClickListener listener;
    private HomeActivity homeActivity;
    private BookListActivity bookListActivity;
    private boolean showFavoriteIcon;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public BookAdapter(List<Book> bookList, OnItemClickListener listener, Context context) {
        this.bookList = bookList;
        this.listener = listener;
        this.showFavoriteIcon = false;
        this.context = context;
    }

    public BookAdapter(List<Book> bookList, OnItemClickListener listener, HomeActivity homeActivity, boolean showFavoriteIcon) {
        this.bookList = bookList;
        this.listener = listener;
        this.homeActivity = homeActivity;
        this.showFavoriteIcon = showFavoriteIcon;
        this.context = homeActivity;
    }

    public BookAdapter(List<Book> bookList, OnItemClickListener listener, BookListActivity bookListActivity, boolean showFavoriteIcon) {
        this.bookList = bookList;
        this.listener = listener;
        this.bookListActivity = bookListActivity;
        this.showFavoriteIcon = showFavoriteIcon;
        this.context = bookListActivity;
    }

    public void updateData(List<Book> newBooks) {
        this.bookList = newBooks;
        Log.d(TAG, "Updated book list size: " + (bookList != null ? bookList.size() : 0));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        if (bookList != null && position < bookList.size()) {
            Book book = bookList.get(position);
            Log.d(TAG, "Binding book at position " + position + ": " + (book != null ? book.getTitle() : "null"));
            holder.bind(book, listener);
        }
    }

    @Override
    public int getItemCount() {
        int count = bookList != null ? bookList.size() : 0;
        Log.d(TAG, "Item count: " + count);
        return count;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgBookCover;
        private TextView txtBookTitle;
        private ImageView btnEdit;
        private ImageView btnDelete;
        private ImageView btnFavorite;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBookCover = itemView.findViewById(R.id.imgBookCover);
            txtBookTitle = itemView.findViewById(R.id.txtBookTitle);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        public void bind(final Book book, final OnItemClickListener listener) {
            if (book == null) {
                Log.e(TAG, "Book is null at position: " + getAdapterPosition());
                return;
            }

            txtBookTitle.setText(book.getTitle() != null ? book.getTitle() : "Không có tiêu đề");
            if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(book.getCoverImage())
                        .placeholder(R.drawable.noimage)
                        .into(imgBookCover);
            } else {
                imgBookCover.setImageResource(R.drawable.noimage);
            }

            View.OnClickListener openDetail = v -> {
                Log.d(TAG, "Clicked on book: " + book.getTitle());
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra("book_id", book.getBookId());
                context.startActivity(intent);
            };
            imgBookCover.setOnClickListener(openDetail);
            txtBookTitle.setOnClickListener(openDetail);

            if (showFavoriteIcon) {
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnFavorite.setVisibility(View.VISIBLE);

                List<Integer> favoriteBookIds = null;
                if (homeActivity != null) {
                    favoriteBookIds = homeActivity.getFavoriteBookIds();
                } else if (bookListActivity != null) {
                    favoriteBookIds = bookListActivity.getFavoriteBookIds();
                }

                if (favoriteBookIds != null && favoriteBookIds.contains(book.getBookId())) {
                    btnFavorite.setImageResource(R.drawable.baseline_bookmark_24);
                } else {
                    btnFavorite.setImageResource(R.drawable.icon_ionic_ios_bookmark);
                }

//                btnFavorite.setOnClickListener(v -> {
//                    if (favoriteBookIds != null) {
//                        if (favoriteBookIds.contains(book.getBookId())) {
//                            if (homeActivity != null) {
//                                homeActivity.removeFromFavorites(book.getBookId());
//                            } else if (bookListActivity != null) {
//                                bookListActivity.removeFromFavorites(book.getBookId());
//                            }
//                        } else {
//                            if (homeActivity != null) {
//                                homeActivity.addToFavorites(book.getBookId());
//                            } else if (bookListActivity != null) {
//                                bookListActivity.addToFavorites(book.getBookId());
//                            }
//                        }
//                        notifyDataSetChanged();
//                    }
//                });
            } else {
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnFavorite.setVisibility(View.GONE);

                btnEdit.setOnClickListener(v -> {
                    Log.d(TAG, "Clicked on edit button for book: " + book.getTitle());
                    Intent intent = new Intent(context, AddEditBookActivity.class);
                    intent.putExtra("book_id", book.getBookId());
                    context.startActivity(intent);
                });

                btnDelete.setOnClickListener(v -> {
                    Log.d(TAG, "Clicked on delete button for book: " + book.getTitle());
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc muốn xóa cuốn sách '" + book.getTitle() + "' không?")
                            .setPositiveButton("Có", (dialog, which) -> {
                                BookController bookController = new BookController(new BookDAO(context));
                                if (bookController.deleteBook(book.getBookId())) {
                                    int position = getAdapterPosition();
                                    if (position != RecyclerView.NO_POSITION) {
                                        bookList.remove(position);
                                        notifyItemRemoved(position);
                                        if (bookListActivity != null) {
                                            bookListActivity.loadBooks();
                                        } else if (homeActivity != null) {
                                            homeActivity.loadData();
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "Failed to delete book with id: " + book.getBookId());
                                }
                            })
                            .setNegativeButton("Không", null)
                            .show();
                });
            }
        }
    }
}