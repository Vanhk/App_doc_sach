package com.example.appdocsachv2.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.BookController;
import com.example.appdocsachv2.controller.ChapterController;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;
import com.example.appdocsachv2.model.ChapterDAO;
import com.example.appdocsachv2.model.ReadingProgressDAO;
import com.example.appdocsachv2.utils.SessionManager;
import com.example.appdocsachv2.view.adapter.BookAdapter;

import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {
    private static final String TAG = "BookDetailActivity";
    private ImageButton btnBack;
    private ImageView imgBook, imgFavorite;
    private TextView txtTenSach, txtTacGia, txtTheLoai, txtNoiDungTomTat;
    private RecyclerView rvSach;
    private BookAdapter relatedBooksAdapter;
    private List<Book> relatedBooksList;
    private BookController bookController;
    private ChapterController chapterController;
    private ReadingProgressDAO readingProgressDAO;
    private SessionManager sessionManager;
    private int bookId;
    private Button btnTiepTuc, btnDocTuDau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        imgBook = findViewById(R.id.imgBook);
        imgFavorite = findViewById(R.id.imagefavorite);
        txtTenSach = findViewById(R.id.txtTenSach);
        txtTacGia = findViewById(R.id.txtTacGia);
        txtTheLoai = findViewById(R.id.txtTheLoai);
        txtNoiDungTomTat = findViewById(R.id.txtNoiDungTomTat);
        rvSach = findViewById(R.id.rvSach);
        btnTiepTuc = findViewById(R.id.btnTiepTuc);
        btnDocTuDau = findViewById(R.id.btnDocTuDau);

        // Khởi tạo controller và DAO
        bookController = new BookController(new BookDAO(this));
        chapterController = new ChapterController(new ChapterDAO(this));
        readingProgressDAO = new ReadingProgressDAO(this);
        sessionManager = new SessionManager(this);

        // Khởi tạo danh sách sách liên quan
        relatedBooksList = new ArrayList<>();
        relatedBooksAdapter = new BookAdapter(relatedBooksList, book -> {
            Intent intent = new Intent(BookDetailActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            startActivity(intent);
        }, this);
        rvSach.setLayoutManager(new GridLayoutManager(this, 3));
        rvSach.setAdapter(relatedBooksAdapter);

        // Lấy bookId từ Intent
        bookId = getIntent().getIntExtra("book_id", -1);
        Log.d(TAG, "Received book_id: " + bookId);

        if (bookId != -1) {
            loadBookDetails();
            loadRelatedBooks();
        } else {
            Log.e(TAG, "Invalid book_id received");
            finish();
        }

        // Sự kiện quay lại
        btnBack.setOnClickListener(v -> onBackPressed());

//        // Sự kiện "Tiếp tục đọc"
//        btnTiepTuc.setOnClickListener(v -> {
//            int userId = sessionManager.getUserId();
//            if (userId != -1) {
//                int lastReadChapterId = readingProgressDAO.getLastReadChapterId(userId, bookId);
//                Intent intent = new Intent(BookDetailActivity.this, ReadBookActivity.class);
//                intent.putExtra("book_id", bookId);
//                intent.putExtra("chapter_id", lastReadChapterId != -1 ? lastReadChapterId : 1);
//                startActivity(intent);
//            } else {
//                Log.e(TAG, "User ID not found");
//            }
//        });

        // Sự kiện "Đọc từ đầu"
        btnDocTuDau.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ChapterListActivity.class);
            intent.putExtra("book_id", bookId);
            startActivity(intent);
        });

        // Xử lý icon yêu thích
        updateFavoriteIcon();
        imgFavorite.setOnClickListener(v -> {
            if (bookController.isFavorite(bookId)) {
                bookController.removeFavorite(bookId);
            } else {
                bookController.addFavorite(bookId);
            }
            updateFavoriteIcon();
        });
    }

    private void loadBookDetails() {
        Book book = bookController.getBookById(bookId);
        if (book != null) {
            txtTenSach.setText(book.getTitle() != null ? book.getTitle() : "Không có tiêu đề");
            txtTacGia.setText(book.getAuthor() != null ? book.getAuthor() : "Không có tác giả");
            txtTheLoai.setText(book.getGenre() != null ? book.getGenre() : "Không có thể loại");
            txtNoiDungTomTat.setText(book.getSummary() != null ? book.getSummary() : "Không có tóm tắt");

            if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
                Glide.with(this).load(book.getCoverImage()).placeholder(R.drawable.noimage).into(imgBook);
            } else {
                imgBook.setImageResource(R.drawable.noimage);
            }
        } else {
            Log.e(TAG, "Book not found for book_id: " + bookId);
            finish();
        }
    }

    private void loadRelatedBooks() {
        Book currentBook = bookController.getBookById(bookId);
        if (currentBook != null && currentBook.getGenre() != null) {
            relatedBooksList.clear();
            List<Book> allBooks = bookController.getAllBooks();
            if (allBooks != null) {
                int count = 0;
                for (Book book : allBooks) {
                    if (book != null && book.getGenre() != null && book.getGenre().equals(currentBook.getGenre()) &&
                            book.getBookId() != currentBook.getBookId() && count < 6) {
                        relatedBooksList.add(book);
                        count++;
                    }
                }
                relatedBooksAdapter.notifyDataSetChanged();
            } else {
                Log.w(TAG, "No books found in database");
            }
        } else {
            Log.w(TAG, "Current book or genre is null");
        }
    }

    private void updateFavoriteIcon() {
        imgFavorite.setImageResource(bookController.isFavorite(bookId) ?
                R.drawable.baseline_bookmark_24 : R.drawable.icon_ionic_ios_bookmark);
    }
}