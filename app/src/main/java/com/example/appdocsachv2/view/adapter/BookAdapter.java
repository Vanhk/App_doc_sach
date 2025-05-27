package com.example.appdocsachv2.view.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.BookController;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.view.activity.AddEditBookActivity;
import com.example.appdocsachv2.view.activity.BookDetailActivity;
import com.example.appdocsachv2.view.activity.BookListActivity;
import com.example.appdocsachv2.view.activity.ChapterListActivity;
import com.example.appdocsachv2.view.activity.HomeActivity;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private static final String TAG = "BookAdapter";
    private List<Book> bookList;
    private OnItemClickListener listener;
    private HomeActivity homeActivity;
    private BookListActivity bookListActivity;
    private BookDetailActivity bookDetailActivity;
    private boolean showFavoriteIcon;
    private Context context;
    private BookController bookController;
    private int userId;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

//    public BookAdapter(List<Book> bookList, OnItemClickListener listener, Context context) {
//        this.bookList = bookList;
//        this.listener = listener;
//        this.showFavoriteIcon = false;
//        this.context = context;
//    }

    public BookAdapter(List<Book> bookList, OnItemClickListener listener, HomeActivity homeActivity, boolean showFavoriteIcon, BookController bookController, int userId) {
        this.bookList = bookList;
        this.listener = listener;
        this.homeActivity = homeActivity;
        this.showFavoriteIcon = showFavoriteIcon;
        this.context = homeActivity;
        this.bookController = bookController;
        this.userId = userId;
    }

    public BookAdapter(List<Book> bookList, OnItemClickListener listener, BookListActivity bookListActivity, boolean showFavoriteIcon, BookController bookController, int userId) {
        this.bookList = bookList;
        this.listener = listener;
        this.bookListActivity = bookListActivity;
        this.showFavoriteIcon = showFavoriteIcon;
        this.context = bookListActivity;
        this.bookController = bookController;
        this.userId = userId;
    }

    public BookAdapter(List<Book> bookList, OnItemClickListener listener, BookDetailActivity bookDetailActivity, boolean showFavoriteIcon, BookController bookController, int userId) {
        this.bookList = bookList;
        this.listener = listener;
        this.bookDetailActivity = bookDetailActivity;
        this.showFavoriteIcon = showFavoriteIcon;
        this.context = bookDetailActivity;
        this.bookController = bookController;
        this.userId = userId;
    }
    //Cập nhật danh sách sách và làm mới RecyclerView
    public void updateData(List<Book> newBooks) {
        this.bookList = newBooks;
        notifyDataSetChanged();
    }
    //Tạo mới một ViewHolder cho item sách
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }
    //Gán dữ liệu sách vào ViewHolder tại vị trí position
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        if (bookList != null && position < bookList.size()) {
            Book book = bookList.get(position);
            holder.bind(book, listener);
        }
    }
    //Trả về số lượng item trong RecyclerView
    @Override
    public int getItemCount() {
        int count = bookList != null ? bookList.size() : 0;
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
                if (listener != null) listener.onItemClick(book);
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra("book_id", book.getBookId());
                context.startActivity(intent);
            };
            //mở bookdetail khi click
            imgBookCover.setOnClickListener(openDetail);
            txtBookTitle.setOnClickListener(openDetail);
            //nếu showFavoriteIcon = true Ẩn btnEdit và btnDelete, hiển thị btnFavorite
            if (showFavoriteIcon) {
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnFavorite.setVisibility(View.VISIBLE);

                final int position = getAdapterPosition();
                btnFavorite.setOnClickListener(v -> {
                    if (position != RecyclerView.NO_POSITION) {
                        List<Integer> favoriteBookIds = null;
                        if (homeActivity != null) {
                            favoriteBookIds = homeActivity.getFavoriteBookIds();
                        } else if (bookListActivity != null) {
                            favoriteBookIds = bookListActivity.getFavoriteBookIds();
                        } else if (bookDetailActivity != null) {
                            favoriteBookIds = bookDetailActivity.getFavoriteBookIds();
                        }

                        if (favoriteBookIds != null) {
                            if (favoriteBookIds.contains(book.getBookId())) {
                                if (homeActivity != null) {
                                    homeActivity.removeFromFavorites(book.getBookId());
                                } else if (bookListActivity != null) {
                                    bookListActivity.removeFromFavorites(book.getBookId());
                                } else if (bookDetailActivity != null) {
                                    bookDetailActivity.removeFromFavorites(book.getBookId());
                                }
                                btnFavorite.setImageResource(R.drawable.icon_ionic_ios_bookmark);
                            } else {
                                if (homeActivity != null) {
                                    homeActivity.addToFavorites(book.getBookId());
                                } else if (bookListActivity != null) {
                                    bookListActivity.addToFavorites(book.getBookId());
                                } else if (bookDetailActivity != null) {
                                    bookDetailActivity.addToFavorites(book.getBookId());
                                }
                                btnFavorite.setImageResource(R.drawable.baseline_bookmark_24);
                            }

                            // Gửi broadcast thông báo thay đổi trạng thái yêu thích
                            Intent broadcastIntent = new Intent(ChapterListActivity.ACTION_FAVORITE_CHANGED);
                            broadcastIntent.putExtra(ChapterListActivity.EXTRA_BOOK_ID, book.getBookId());
                            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);

                            notifyItemChanged(position);
                        }
                    }
                });

                // Cập nhật trạng thái icon ngay khi bind
                List<Integer> favoriteBookIds = null;
                if (homeActivity != null) {
                    favoriteBookIds = homeActivity.getFavoriteBookIds();
                } else if (bookListActivity != null) {
                    favoriteBookIds = bookListActivity.getFavoriteBookIds();
                } else if (bookDetailActivity != null) {
                    favoriteBookIds = bookDetailActivity.getFavoriteBookIds();
                }
                if (favoriteBookIds != null && favoriteBookIds.contains(book.getBookId())) {
                    btnFavorite.setImageResource(R.drawable.baseline_bookmark_24);
                } else {
                    btnFavorite.setImageResource(R.drawable.icon_ionic_ios_bookmark);
                }
            } else {//Hiển thị btnEdit và btnDelete, ẩn btnFavorite
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnFavorite.setVisibility(View.GONE);
                //mở addeditactivity
                btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(context, AddEditBookActivity.class);
                    intent.putExtra("book_id", book.getBookId());
                    context.startActivity(intent);
                });
                //nút xóa
                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc muốn xóa cuốn sách '" + book.getTitle() + "' không?")
                            .setPositiveButton("Có", (dialog, which) -> {
                                if (bookController != null) {
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
                                    }
                                }
                            })
                            .setNegativeButton("Không", null)
                            .show();
                });
            }
        }
    }
}