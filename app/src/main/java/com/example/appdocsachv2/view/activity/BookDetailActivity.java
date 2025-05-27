package com.example.appdocsachv2.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.BookController;
import com.example.appdocsachv2.controller.ChapterController;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;
import com.example.appdocsachv2.model.Chapter;
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
    private int userId;
    private String listType;
    private boolean fromHome;
    private Book currentBook;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "Favorites";
    private static final String FAVORITES_KEY = "favorite_book_ids_user_";
    public static final String ACTION_FAVORITE_CHANGED = "com.example.appdocsachv2.FAVORITE_CHANGED";
    public static final String EXTRA_BOOK_ID = "book_id";

//Lắng nghe thay đổi trạng thái yêu thích từ các Activity khác và cập nhật giao diện
    private BroadcastReceiver favoriteChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int changedBookId = intent.getIntExtra(EXTRA_BOOK_ID, -1);
            if (changedBookId == bookId) {
                updateFavoriteIcon();
                loadRelatedBooks();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Ánh xạ view
        mapViews();

        // Khởi tạo controller và DAO
        initializeControllers();

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Lấy thông tin từ Intent
        getIntentData();

        // Khởi tạo adapter danh sách sách liên quan
        setupRelatedBooksAdapter();

        // Lấy bookId và hiển thị nội dung sách
        bookId = getIntent().getIntExtra("book_id", -1);
        if (bookId != -1) {
            currentBook = bookController.getBookById(bookId);
            if (currentBook != null) {
                displayBookDetails();
                loadRelatedBooks();
            } else {
                finish();
            }
        } else {
            finish();
        }

        // Xử lý sự kiện
        setupEvents();
    }

    private void mapViews() {
        btnBack = findViewById(R.id.btnBackdetails);
        imgBook = findViewById(R.id.imgBook);
        imgFavorite = findViewById(R.id.imagefavorite);
        txtTenSach = findViewById(R.id.txtTenSach);
        txtTacGia = findViewById(R.id.txtTacGia);
        txtTheLoai = findViewById(R.id.txtTheLoai);
        txtNoiDungTomTat = findViewById(R.id.txtNoiDungTomTat);
        rvSach = findViewById(R.id.rvSach);
        btnTiepTuc = findViewById(R.id.btnTiepTuc);
        btnDocTuDau = findViewById(R.id.btnDocTuDau);
    }

    private void initializeControllers() {
        bookController = new BookController(new BookDAO(this));
        chapterController = new ChapterController(new ChapterDAO(this));
        readingProgressDAO = new ReadingProgressDAO(this);
        sessionManager = new SessionManager(this);
        userId = getIntent().getIntExtra("user_id", sessionManager.getUserId());
        if (userId == -1) {
            userId = 1;
        }
    }
//Lấy listType và fromHome từ Intent
    private void getIntentData() {
        listType = getIntent().getStringExtra("list_type");
        if (listType == null) listType = "default";
        fromHome = getIntent().getBooleanExtra("fromHome", false);
    }
//Thiết lập RecyclerView với GridLayoutManager (3 cột) và adapter cho sách liên quan
    private void setupRelatedBooksAdapter() {
        relatedBooksList = new ArrayList<>();
        relatedBooksAdapter = new BookAdapter(relatedBooksList, book -> {
            navigateToBookDetail(book.getBookId());
        }, this, true, bookController, userId);
        rvSach.setLayoutManager(new GridLayoutManager(this, 3));
        rvSach.setAdapter(relatedBooksAdapter);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> navigateBack());

        btnTiepTuc.setOnClickListener(v -> {
            int lastReadPage = readingProgressDAO.getLastReadPage(userId, bookId);
            navigateToReadBook(bookId, lastReadPage != -1 ? lastReadPage : 0); // Sửa mặc định thành 0
        });

        btnDocTuDau.setOnClickListener(v -> {
            List<Chapter> chapters = chapterController.getChaptersByBookId(bookId);
            if (chapters.isEmpty()) {
                navigateToReadBook(bookId, 0);
            } else {
                navigateToChapterList(bookId);
            }
        });

        imgFavorite.setOnClickListener(v -> {
            if (isFavorite(bookId)) {
                removeFromFavorites(bookId);
                Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                addToFavorites(bookId);
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteIcon();
            loadRelatedBooks();

            // Gửi broadcast để thông báo thay đổi trạng thái yêu thích
            Intent broadcastIntent = new Intent(ACTION_FAVORITE_CHANGED);
            broadcastIntent.putExtra(EXTRA_BOOK_ID, bookId);
            LocalBroadcastManager.getInstance(BookDetailActivity.this).sendBroadcast(broadcastIntent);
        });

        updateFavoriteIcon();
    }

    private void displayBookDetails() {
        txtTenSach.setText(nonNull(currentBook.getTitle(), "Không có tiêu đề"));
        txtTacGia.setText(nonNull(currentBook.getAuthor(), "Không có tác giả"));
        txtTheLoai.setText(nonNull(currentBook.getGenre(), "Không có thể loại"));
        txtNoiDungTomTat.setText(nonNull(currentBook.getSummary(), "Không có tóm tắt"));
// sử dụng Glide để tải ảnh bìa
        if (currentBook.getCoverImage() != null && !currentBook.getCoverImage().isEmpty()) {
            Glide.with(this).load(currentBook.getCoverImage()).placeholder(R.drawable.noimage).into(imgBook);
        } else {
            imgBook.setImageResource(R.drawable.noimage);
        }
    }
//lấy tối đa 6 sách cùng thể loại
    private void loadRelatedBooks() {
        if (currentBook != null && currentBook.getGenre() != null) {
            relatedBooksList.clear();
            List<Book> allBooks = bookController.getAllBooks();
            int count = 0;
            for (Book book : allBooks) {
                if (book != null && book.getGenre() != null &&
                        book.getGenre().equals(currentBook.getGenre()) &&
                        book.getBookId() != currentBook.getBookId() && count < 6) {
                    relatedBooksList.add(book);
                    count++;
                }
            }
            relatedBooksAdapter.updateData(relatedBooksList);
        }
    }

    private String nonNull(String value, String fallback) {
        return value != null ? value : fallback;
    }

    private void updateFavoriteIcon() {
        imgFavorite.setImageResource(isFavorite(bookId) ?
                R.drawable.baseline_bookmark_24 : R.drawable.icon_ionic_ios_bookmark);
    }

    private boolean isFavorite(int bookId) {
        return getFavoriteBookIds().contains(bookId);
    }

    public List<Integer> getFavoriteBookIds() {
        String favoriteIds = sharedPreferences.getString(FAVORITES_KEY + userId, "");
        List<Integer> bookIds = new ArrayList<>();
        if (!favoriteIds.isEmpty()) {
            for (String id : favoriteIds.split(",")) {
                try {
                    bookIds.add(Integer.parseInt(id.trim()));
                } catch (NumberFormatException e) {
                }
            }
        }
        return bookIds;
    }

    public void addToFavorites(int bookId) {
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (!favoriteBookIds.contains(bookId)) {
            favoriteBookIds.add(bookId);
            saveFavoriteBookIds(favoriteBookIds);
        }
    }

    public void removeFromFavorites(int bookId) {
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (favoriteBookIds.contains(bookId)) {
            favoriteBookIds.remove(Integer.valueOf(bookId));
            saveFavoriteBookIds(favoriteBookIds);
        }
    }

    private void saveFavoriteBookIds(List<Integer> bookIds) {
        String ids = android.text.TextUtils.join(",", bookIds);
        sharedPreferences.edit().putString(FAVORITES_KEY + userId, ids).apply();
    }

    // Navigation helper methods
// mở chi tiết sách khi click vào sách khác cùng thể loại
    private void navigateToBookDetail(int bookId) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("book_id", bookId);
        intent.putExtra("list_type", listType);
        intent.putExtra("fromHome", fromHome);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void navigateBack() {
        if (fromHome) {
            // Nếu đến từ HomeActivity, quay lại HomeActivity
            Intent intent = new Intent(BookDetailActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            // Nếu không đến từ HomeActivity, quay lại BookListActivity với listType tương ứng
            Intent intent = new Intent(this, BookListActivity.class);
            intent.putExtra("list_type", listType);
            intent.putExtra("user_id", userId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }
//Chuyển đến ReadBookActivity với trang bắt đầu
    private void navigateToReadBook(int bookId, int chapterIdOrPage) {
        Book book = bookController.getBookById(bookId);
        if (book == null) {
            Toast.makeText(this, "Không tìm thấy sách", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ReadBookActivity.class);
        intent.putExtra("book_id", bookId);
        intent.putExtra("start_page", chapterIdOrPage);
        intent.putExtra("user_id", userId);
        intent.putExtra("pdf_path", book.getFilePath());
        intent.putExtra("book_title", book.getTitle());
        intent.putExtra("total_pages", book.getTotal_pages());
        startActivity(intent);
    }
//chuyển đến chapterlist
    private void navigateToChapterList(int bookId) {
        Book book = bookController.getBookById(bookId);
        if (book == null) {
            Toast.makeText(this, "Không tìm thấy sách", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ChapterListActivity.class);
        intent.putExtra("book_id", bookId);
        intent.putExtra("user_id", userId);
        intent.putExtra("pdf_path", book.getFilePath());
        intent.putExtra("book_title", book.getTitle());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFavoriteIcon();
        loadRelatedBooks();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(favoriteChangeReceiver,
                new IntentFilter(ACTION_FAVORITE_CHANGED));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(favoriteChangeReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateFavoriteIcon();
    }
}