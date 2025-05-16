package com.example.appdocsachv2.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.BookController;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;
import com.example.appdocsachv2.model.ReadingProgress;
import com.example.appdocsachv2.model.ReadingProgressDAO;
import com.example.appdocsachv2.utils.SessionManager;
import com.example.appdocsachv2.view.adapter.BookAdapter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private RecyclerView recyclerMyBooks, recyclerReadingProgress, recyclerMyFavoriteBooks;
    private BookAdapter myBooksAdapter, readingProgressAdapter, favoriteBooksAdapter;
    private List<Book> myBooksList, readingProgressList, favoriteBooksList;
    private BookController bookController;
    private ReadingProgressDAO readingProgressDAO;
    private NavigationView navigationView;
    private SharedPreferences sharedPreferences;
    private int userId;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private AutoCompleteTextView searchBar;
    private List<String> suggestionList;

    private BroadcastReceiver favoriteChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received favorite change broadcast");
            loadData(); // Tải lại dữ liệu để cập nhật danh sách yêu thích
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        userId = sessionManager.getUserId();
        Log.d(TAG, "User ID retrieved: " + userId);

        setContentView(R.layout.activity_home);

        // Đăng ký BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(favoriteChangeReceiver,
                new IntentFilter(ChapterListActivity.ACTION_FAVORITE_CHANGED));

        // Khởi tạo các view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchBar = findViewById(R.id.search_bar);
        recyclerMyBooks = findViewById(R.id.recyclerMyBooks);
        recyclerReadingProgress = findViewById(R.id.recyclerReadingProgress);
        recyclerMyFavoriteBooks = findViewById(R.id.recyclerMyFavoriteBooks);
        ImageView btAddHome = findViewById(R.id.bt_addhome);

        // Khởi tạo controller và DAO
        BookDAO bookDAO = new BookDAO(this);
        bookController = new BookController(bookDAO);
        readingProgressDAO = new ReadingProgressDAO(this);
        sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);

        // Khởi tạo suggestionList
        suggestionList = new ArrayList<>();
        updateSuggestions();
        ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestionList);
        searchBar.setAdapter(suggestionAdapter);

        // Cấu hình RecyclerView
        LinearLayoutManager myBooksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerMyBooks.setLayoutManager(myBooksLayoutManager);
        myBooksList = new ArrayList<>();
        myBooksAdapter = new BookAdapter(myBooksList, book -> {
            Intent intent = new Intent(HomeActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            intent.putExtra("list_type", "my_books");
            intent.putExtra("fromHome", true);
            intent.putExtra("user_id", userId);
            Log.d(TAG, "Navigating to BookDetailActivity from myBooks with book_id: " + book.getBookId() + ", listType: my_books, fromHome: true");
            startActivity(intent);
        }, this, false, bookController, userId);
        recyclerMyBooks.setAdapter(myBooksAdapter);

        LinearLayoutManager readingProgressLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerReadingProgress.setLayoutManager(readingProgressLayoutManager);
        readingProgressList = new ArrayList<>();
        readingProgressAdapter = new BookAdapter(readingProgressList, book -> {
            Intent intent = new Intent(HomeActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            intent.putExtra("list_type", "reading_progress");
            intent.putExtra("fromHome", true);
            intent.putExtra("user_id", userId);
            Log.d(TAG, "Navigating to BookDetailActivity from readingProgress with book_id: " + book.getBookId() + ", listType: reading_progress, fromHome: true");
            startActivity(intent);
        }, this, true, bookController, userId);
        recyclerReadingProgress.setAdapter(readingProgressAdapter);

        LinearLayoutManager favoriteBooksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerMyFavoriteBooks.setLayoutManager(favoriteBooksLayoutManager);
        favoriteBooksList = new ArrayList<>();
        favoriteBooksAdapter = new BookAdapter(favoriteBooksList, book -> {
            Intent intent = new Intent(HomeActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            intent.putExtra("list_type", "favorite_books");
            intent.putExtra("fromHome", true);
            intent.putExtra("user_id", userId);
            Log.d(TAG, "Navigating to BookDetailActivity from favoriteBooks with book_id: " + book.getBookId() + ", listType: favorite_books, fromHome: true");
            startActivity(intent);
        }, this, true, bookController, userId);
        recyclerMyFavoriteBooks.setAdapter(favoriteBooksAdapter);

        // Xử lý tìm kiếm trong searchBar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().toLowerCase();
                List<Book> filteredBooks = new ArrayList<>();
                for (Book book : myBooksList) {
                    if (book.getTitle().toLowerCase().contains(keyword) ||
                            (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(keyword)) ||
                            (book.getGenre() != null && book.getGenre().toLowerCase().contains(keyword))) {
                        filteredBooks.add(book);
                    }
                }
                myBooksAdapter.updateData(filteredBooks);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý khi chọn gợi ý
        searchBar.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSuggestion = (String) parent.getItemAtPosition(position);
            searchBar.setText(selectedSuggestion);
            Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
            intent.putExtra("list_type", "search_results");
            intent.putExtra("search_keyword", selectedSuggestion);
            intent.putExtra("fromSearch", true);
            intent.putExtra("user_id", userId);
            Log.d(TAG, "Navigating to BookListActivity with search keyword: " + selectedSuggestion);
            startActivity(intent);
        });

        // Xử lý tìm kiếm khi nhấn Enter
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String keyword = searchBar.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
                    intent.putExtra("list_type", "search_results");
                    intent.putExtra("search_keyword", keyword);
                    intent.putExtra("fromSearch", true);
                    intent.putExtra("user_id", userId);
                    Log.d(TAG, "Navigating to BookListActivity with search keyword: " + keyword);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });

        // Sự kiện nút Thêm sách
        btAddHome.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEditBookActivity.class);
            intent.putExtra("book_id", -1);
            intent.putExtra("user_id", userId);
            activityResultLauncher.launch(intent);
        });

        TextView tvViewAllMyBook = findViewById(R.id.tv_viewallmybook);
        TextView tvViewAllReadingProgress = findViewById(R.id.tvreading);
        TextView tvViewAllFavoriteBooks = findViewById(R.id.tvfavorite);

        // Sự kiện Xem tất cả
        tvViewAllMyBook.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
            intent.putExtra("list_type", "my_books");
            intent.putExtra("fromSearch", false);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
        tvViewAllReadingProgress.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
            intent.putExtra("list_type", "reading_progress");
            intent.putExtra("fromSearch", false);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
        tvViewAllFavoriteBooks.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
            intent.putExtra("list_type", "favorite_books");
            intent.putExtra("fromSearch", false);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // Khởi tạo DrawerLayout và NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_my_books) {
                Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
                intent.putExtra("list_type", "my_books");
                intent.putExtra("fromSearch", false);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            } else if (id == R.id.nav_continue_reading) {
                Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
                intent.putExtra("list_type", "reading_progress");
                intent.putExtra("fromSearch", false);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            } else if (id == R.id.nav_favorite_books) {
                Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
                intent.putExtra("list_type", "favorite_books");
                intent.putExtra("fromSearch", false);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                Log.d(TAG, "Logging out user " + userId);
                sessionManager.logout();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_settings) {
                // Xử lý settings
            } else if (id == R.id.nav_about) {
                // Hiển thị thông tin nhóm sinh viên và đề tài bằng dialog
                StringBuilder aboutMessage = new StringBuilder();
                aboutMessage.append("TRƯỜNG ĐẠI HỌC CÔNG NGHIỆP HÀ NỘI\n");
                aboutMessage.append("TRƯỜNG CÔNG NGHỆ THÔNG TIN VÀ TRUYỀN THÔNG\n");
                aboutMessage.append("BÁO CÁO THỰC NGHIỆM THUỘC HỌC PHẦN\n");
                aboutMessage.append("PHÁT TRIỂN ỨNG DỤNG TRÊN THIẾT BỊ DI ĐỘNG\n\n");
                aboutMessage.append("ĐỀ TÀI\n");
                aboutMessage.append("XÂY DỰNG ỨNG DỤNG ĐỌC SÁCH\n\n");
                aboutMessage.append("Giáo viên hướng dẫn: ThS. Đỗ Hữu Công\n");
                aboutMessage.append("Lớp: 20242IT6029001\n");
                aboutMessage.append("Nhóm: 8\n");
                aboutMessage.append("Sinh viên thực hiện:\n");
                aboutMessage.append(" - Nguyễn Viết Anh – 2022604934\n");
                aboutMessage.append(" - Nguyễn Hoàng Hiệp – 2022604601\n");
                aboutMessage.append(" - Nguyễn Xuân Nhiên – 2022604916\n");
                aboutMessage.append(" - Phạm Chí Thành – 2022605231\n");
                aboutMessage.append(" - Cao Xuân Sơn – 2022600457\n");

                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Thông tin về ứng dụng")
                        .setMessage(aboutMessage.toString())
                        .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                        .show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Xử lý nút Back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Khởi tạo ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Activity result received, reloading data");
                        loadData();
                    }
                });

        // Load dữ liệu
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called, reloading data");
        loadData();
    }

    public void loadData() {
        Log.d(TAG, "Loading data...");
        // Load Sách của tôi
        myBooksList.clear();
        List<Book> allBooks = bookController.getAllBooks();
        if (allBooks != null) {
            myBooksList.addAll(allBooks);
            Log.d(TAG, "Loaded " + myBooksList.size() + " my books");
        } else {
            Log.e(TAG, "Failed to load all books");
        }
        myBooksAdapter.updateData(myBooksList);

        // Load Sách đọc dở
        readingProgressList.clear();
        List<ReadingProgress> progressList = readingProgressDAO.getReadingProgress(userId);
        Log.d(TAG, "Fetched " + (progressList != null ? progressList.size() : 0) + " reading progress records for userId: " + userId);
        if (progressList != null) {
            for (ReadingProgress progress : progressList) {
                Book book = bookController.getBookById(progress.getBookId());
                if (book != null) {
                    readingProgressList.add(book);
                    Log.d(TAG, "Added book to reading progress: " + book.getTitle());
                } else {
                    Log.e(TAG, "Book not found for progress with bookId: " + progress.getBookId());
                }
            }
            Log.d(TAG, "Loaded " + readingProgressList.size() + " reading progress books");
        } else {
            Log.e(TAG, "Failed to load reading progress");
        }
        readingProgressAdapter.updateData(readingProgressList);

        // Load Sách yêu thích
        favoriteBooksList.clear();
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (favoriteBookIds != null) {
            for (int bookId : favoriteBookIds) {
                Book book = bookController.getBookById(bookId);
                if (book != null) {
                    favoriteBooksList.add(book);
                }
            }
            Log.d(TAG, "Loaded " + favoriteBooksList.size() + " favorite books");
        } else {
            Log.e(TAG, "Failed to load favorite book IDs");
        }
        favoriteBooksAdapter.updateData(favoriteBooksList);

        // Cập nhật danh sách gợi ý
        updateSuggestions();
        ArrayAdapter<String> suggestionAdapter = (ArrayAdapter<String>) searchBar.getAdapter();
        suggestionAdapter.clear();
        suggestionAdapter.addAll(suggestionList);
        suggestionAdapter.notifyDataSetChanged();
    }

    private void updateSuggestions() {
        suggestionList.clear();
        Set<String> suggestions = new HashSet<>();
        List<Book> allBooks = bookController.getAllBooks();
        if (allBooks != null) {
            for (Book book : allBooks) {
                if (book.getTitle() != null && !book.getTitle().isEmpty()) {
                    suggestions.add(book.getTitle());
                }
                if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
                    suggestions.add(book.getAuthor());
                }
                if (book.getGenre() != null && !book.getGenre().isEmpty()) {
                    suggestions.add(book.getGenre());
                }
            }
        }
        suggestionList.addAll(suggestions);
        Collections.sort(suggestionList);
        Log.d(TAG, "Suggestions list size: " + suggestionList.size());
    }

    public List<Integer> getFavoriteBookIds() {
        String favoriteIds = sharedPreferences.getString("favorite_book_ids_user_" + userId, "");
        Log.d(TAG, "Fetched favorite IDs: " + favoriteIds);
        if (favoriteIds.isEmpty()) {
            Log.d(TAG, "No favorite book IDs found for user " + userId);
            return new ArrayList<>();
        }
        String[] ids = favoriteIds.split(",");
        List<Integer> bookIds = new ArrayList<>();
        for (String id : ids) {
            try {
                bookIds.add(Integer.parseInt(id.trim()));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing favorite book id: " + e.getMessage());
            }
        }
        return bookIds;
    }

    public void addToFavorites(int bookId) {
        Log.d(TAG, "Attempting to add bookId " + bookId + " to favorites for user " + userId);
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (!favoriteBookIds.contains(bookId)) {
            favoriteBookIds.add(bookId);
            saveFavoriteBookIds(favoriteBookIds);
            loadData();
            Log.d(TAG, "Added bookId " + bookId + " to favorites, new size: " + favoriteBookIds.size());
        } else {
            Log.d(TAG, "BookId " + bookId + " already in favorites");
        }
    }

    public void removeFromFavorites(int bookId) {
        Log.d(TAG, "Attempting to remove bookId " + bookId + " from favorites for user " + userId);
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (favoriteBookIds.contains(bookId)) {
            favoriteBookIds.remove(Integer.valueOf(bookId));
            saveFavoriteBookIds(favoriteBookIds);
            loadData();
            Log.d(TAG, "Removed bookId " + bookId + " from favorites, new size: " + favoriteBookIds.size());
        } else {
            Log.d(TAG, "BookId " + bookId + " not found in favorites");
        }
    }

    private void saveFavoriteBookIds(List<Integer> bookIds) {
        String ids = android.text.TextUtils.join(",", bookIds);
        sharedPreferences.edit().putString("favorite_book_ids_user_" + userId, ids).apply();
        Log.d(TAG, "Saved favorite book IDs for user " + userId + ": " + ids);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(favoriteChangeReceiver);
    }
}