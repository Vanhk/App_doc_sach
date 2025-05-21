package com.example.appdocsachv2.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.BookController;
import com.example.appdocsachv2.controller.ChapterController;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;
import com.example.appdocsachv2.model.Chapter;
import com.example.appdocsachv2.model.ChapterDAO;
import com.example.appdocsachv2.view.adapter.ChapterAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChapterListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewChapters;
    private ImageView imgBook;
    private ImageButton btnBookMark;
    private ChapterAdapter chapterAdapter;
    private List<Chapter> chapterList;
    private ChapterController chapterController;
    private BookController bookController;
    private int bookId;
    private int selectedChapterPosition = -1;
    private int totalPages = 0;
    private int userId;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "Favorites";
    private static final String FAVORITES_KEY = "favorite_book_ids_user_";
    public static final String ACTION_FAVORITE_CHANGED = "com.example.appdocsachv2.FAVORITE_CHANGED";
    public static final String EXTRA_BOOK_ID = "book_id";

    private BroadcastReceiver favoriteChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int changedBookId = intent.getIntExtra(EXTRA_BOOK_ID, -1);
            if (changedBookId == bookId) {
                String favoriteIds = sharedPreferences.getString(FAVORITES_KEY + userId, "");
                boolean isBookmarked = false;
                if (!favoriteIds.isEmpty()) {
                    String[] ids = favoriteIds.split(",");
                    for (String id : ids) {
                        if (id.trim().equals(String.valueOf(bookId))) {
                            isBookmarked = true;
                            break;
                        }
                    }
                }
                btnBookMark.setImageResource(isBookmarked ? R.drawable.baseline_bookmark_24 : R.drawable.icon_ionic_ios_bookmark);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        recyclerViewChapters = findViewById(R.id.recyclerViewChapters);
        imgBook = findViewById(R.id.imgBook);
        ImageButton btnQuaylai = findViewById(R.id.btnQuaylai);
        btnBookMark = findViewById(R.id.btnBookMark);
        Button btnDoc = findViewById(R.id.button2);

        String pdfPath = getIntent().getStringExtra("pdf_path");
        String bookTitle = getIntent().getStringExtra("book_title");

        if (pdfPath == null || pdfPath.isEmpty() || bookTitle == null) {
            Toast.makeText(this, "Dữ liệu sách không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Đăng ký BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(favoriteChangeReceiver,
                new IntentFilter(ACTION_FAVORITE_CHANGED));

        // Khởi tạo controller
        ChapterDAO chapterDAO = new ChapterDAO(this);
        chapterController = new ChapterController(chapterDAO);

        BookDAO bookDAO = new BookDAO(this);
        bookController = new BookController(bookDAO);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            userId = 0;
        }

        chapterList = new ArrayList<>();
        bookId = getIntent().getIntExtra("book_id", -1);
        if (bookId != -1) {
            chapterList.addAll(chapterController.getChaptersByBookId(bookId));
            Book book = bookController.getBookById(bookId);
            if (book != null) {
                totalPages = book.getTotal_pages();
                String coverImage = book.getCoverImage();
                if (coverImage != null && !coverImage.isEmpty()) {
                    File coverFile = new File(coverImage);
                    if (coverFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(coverFile.getAbsolutePath());
                        if (bitmap != null) {
                            imgBook.setImageBitmap(bitmap);
                        } else {
                            imgBook.setImageResource(R.drawable.sach3);
                        }
                    } else {
                        try {
                            Uri coverUri = Uri.parse(coverImage);
                            imgBook.setImageURI(coverUri);
                        } catch (Exception e) {
                            imgBook.setImageResource(R.drawable.sach3);
                            Toast.makeText(this, "Không thể tải ảnh bìa", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    imgBook.setImageResource(R.drawable.sach3);
                }

                // Kiểm tra trạng thái yêu thích
                String favoriteIds = sharedPreferences.getString(FAVORITES_KEY + userId, "");
                boolean isBookmarked = false;
                if (!favoriteIds.isEmpty()) {
                    String[] ids = favoriteIds.split(",");
                    for (String id : ids) {
                        if (id.trim().equals(String.valueOf(bookId))) {
                            isBookmarked = true;
                            break;
                        }
                    }
                }
                btnBookMark.setImageResource(isBookmarked ? R.drawable.baseline_bookmark_24 : R.drawable.icon_ionic_ios_bookmark);
            }
        }

        if (chapterList.isEmpty()) {
            Intent intent = new Intent(ChapterListActivity.this, ReadBookActivity.class);
            intent.putExtra("book_id", bookId);
            intent.putExtra("chapter_title", "Không có chương");
            intent.putExtra("start_page", 0);
            intent.putExtra("end_page", totalPages > 0 ? totalPages - 1 : 0);
            intent.putExtra("pdf_path", pdfPath);
            intent.putExtra("book_title", bookTitle);
            intent.putExtra("total_pages", totalPages);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
            return;
        }

        chapterAdapter = new ChapterAdapter(chapterList, this::onChapterClick);
        recyclerViewChapters.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChapters.setAdapter(chapterAdapter);

        btnQuaylai.setOnClickListener(v -> onBackPressed());

        btnBookMark.setOnClickListener(v -> {
            String favoriteIds = sharedPreferences.getString(FAVORITES_KEY + userId, "");
            List<String> favoriteBookIdsList = new ArrayList<>();
            if (!favoriteIds.isEmpty()) {
                favoriteBookIdsList.addAll(Arrays.asList(favoriteIds.split(",")));
            }
            boolean isBookmarked = favoriteBookIdsList.contains(String.valueOf(bookId));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isBookmarked) {
                favoriteBookIdsList.remove(String.valueOf(bookId));
                editor.putString(FAVORITES_KEY + userId, String.join(",", favoriteBookIdsList));
                editor.apply();
                btnBookMark.setImageResource(R.drawable.icon_ionic_ios_bookmark);
                Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                favoriteBookIdsList.add(String.valueOf(bookId));
                editor.putString(FAVORITES_KEY + userId, String.join(",", favoriteBookIdsList));
                editor.apply();
                btnBookMark.setImageResource(R.drawable.baseline_bookmark_24);
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }

            Intent broadcastIntent = new Intent(ACTION_FAVORITE_CHANGED);
            broadcastIntent.putExtra(EXTRA_BOOK_ID, bookId);
            LocalBroadcastManager.getInstance(ChapterListActivity.this).sendBroadcast(broadcastIntent);
        });

        btnDoc.setOnClickListener(v -> {
            Intent readIntent = new Intent(ChapterListActivity.this, ReadBookActivity.class);
            readIntent.putExtra("book_id", bookId);
            readIntent.putExtra("pdf_path", pdfPath);
            readIntent.putExtra("book_title", bookTitle);
            readIntent.putExtra("total_pages", totalPages);
            readIntent.putExtra("user_id", userId);

            int startPage;
            int endPage;
            String chapterTitle;

            if (selectedChapterPosition >= 0 && selectedChapterPosition < chapterList.size()) {
                Chapter chapter = chapterList.get(selectedChapterPosition);
                startPage = safePageIndex(chapter.getStartPage());
                endPage = safePageIndex(chapter.getEndPage());
                chapterTitle = chapter.getTitle(); // Lấy tiêu đề trực tiếp từ chương đã chọn
            } else {
                startPage = 0;
                endPage = totalPages > 0 ? totalPages - 1 : 0;
                chapterTitle = "Đọc từ đầu"; // Tiêu đề mặc định khi chưa chọn chương
            }

            readIntent.putExtra("chapter_title", chapterTitle);
            readIntent.putExtra("start_page", startPage);
            readIntent.putExtra("end_page", endPage);
            startActivity(readIntent);
        });
    }

    private String determineChapterTitleByPage(int pageIndex) {
        if (chapterList == null || chapterList.isEmpty()) {
            return "Không có chương";
        }

        for (Chapter chapter : chapterList) {
            if (pageIndex >= chapter.getStartPage() && pageIndex <= chapter.getEndPage()) {
                return chapter.getTitle();
            }
        }
        return "Chưa thuộc chương nào";
    }

    private void onChapterClick(Chapter chapter) {
        selectedChapterPosition = chapterList.indexOf(chapter);
        chapterAdapter.setSelectedPosition(selectedChapterPosition);
        Button btnDoc = findViewById(R.id.button2);
        btnDoc.setEnabled(true);
    }

    private int safePageIndex(int page) {
        if (totalPages <= 0) return 0;
        return Math.max(0, Math.min(page, totalPages - 1));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(favoriteChangeReceiver);
    }
}