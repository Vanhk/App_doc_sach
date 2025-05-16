package com.example.appdocsachv2.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.BookController;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;
import com.example.appdocsachv2.model.ReadingProgress;
import com.example.appdocsachv2.model.ReadingProgressDAO;
import com.example.appdocsachv2.view.adapter.BookAdapter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private RecyclerView recyclerMyBooks, recyclerReadingProgress, recyclerMyFavoriteBooks;
    private BookAdapter myBooksAdapter, readingProgressAdapter, favoriteBooksAdapter;
    private List<Book> myBooksList, readingProgressList, favoriteBooksList;
    private BookController bookController;
    private ReadingProgressDAO readingProgressDAO;
    private NavigationView navigationView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo các view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().toLowerCase();
                List<Book> filteredBooks = new ArrayList<>();
                for (Book book : myBooksList) {
                    if (book.getTitle().toLowerCase().contains(keyword)
                            || book.getAuthor().toLowerCase().contains(keyword)) {
                        filteredBooks.add(book);
                    }
                }
                myBooksAdapter.updateData(filteredBooks);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        recyclerMyBooks = findViewById(R.id.recyclerMyBooks);
        recyclerReadingProgress = findViewById(R.id.recyclerReadingProgress);
        recyclerMyFavoriteBooks = findViewById(R.id.recyclerMyFavoriteBooks);
        ImageView btAddHome = findViewById(R.id.bt_addhome);

        // Khởi tạo controller và DAO
        BookDAO bookDAO = new BookDAO(this);
        bookController = new BookController(bookDAO);
        readingProgressDAO = new ReadingProgressDAO(this);
        sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);

        // Cấu hình RecyclerView
        LinearLayoutManager myBooksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerMyBooks.setLayoutManager(myBooksLayoutManager);
        myBooksList = new ArrayList<>();
        myBooksAdapter = new BookAdapter(myBooksList, book -> {
            Intent intent = new Intent(HomeActivity.this, AddEditBookActivity.class);
            intent.putExtra("book_id", book.getBookId());
            startActivity(intent);
        }, this, false); // Không hiển thị icon yêu thích, dùng icon sửa/xóa
        recyclerMyBooks.setAdapter(myBooksAdapter);

        LinearLayoutManager readingProgressLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerReadingProgress.setLayoutManager(readingProgressLayoutManager);
        readingProgressList = new ArrayList<>();
        readingProgressAdapter = new BookAdapter(readingProgressList, book -> {
            Intent intent = new Intent(HomeActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            startActivity(intent);
        }, this, true); // Hiển thị icon yêu thích
        recyclerReadingProgress.setAdapter(readingProgressAdapter);

        LinearLayoutManager favoriteBooksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerMyFavoriteBooks.setLayoutManager(favoriteBooksLayoutManager);
        favoriteBooksList = new ArrayList<>();
        favoriteBooksAdapter = new BookAdapter(favoriteBooksList, book -> {
            Intent intent = new Intent(HomeActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            startActivity(intent);
        }, this, true); // Hiển thị icon yêu thích
        recyclerMyFavoriteBooks.setAdapter(favoriteBooksAdapter);

        // Sự kiện nút Thêm sách
        btAddHome.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEditBookActivity.class);
            intent.putExtra("book_id", -1);
            startActivity(intent);
        });

        TextView tvViewAllMyBook = findViewById(R.id.tv_viewallmybook);
        TextView tvViewAllReadingProgress = findViewById(R.id.tvreading);
        TextView tvViewAllFavoriteBooks = findViewById(R.id.tvfavorite);

        // Sự kiện Xem tất cả Sách của tôi
        tvViewAllMyBook.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
            intent.putExtra("list_type", "my_books");
            startActivity(intent);
        });

        // Sự kiện Xem tất cả Đọc tiếp
        tvViewAllReadingProgress.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
            intent.putExtra("list_type", "reading_progress");
            startActivity(intent);
        });

        // Sự kiện Xem tất cả Sách yêu thích
        tvViewAllFavoriteBooks.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookListActivity.class);
            intent.putExtra("list_type", "favorite_books");
            startActivity(intent);
        });

        // Khởi tạo DrawerLayout và NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Cấu hình DrawerToggle với Toolbar
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Xử lý sự kiện khi chọn mục trong Navigation Drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_my_books) {
                // Xử lý "Sách của tôi"
            } else if (id == R.id.nav_continue_reading) {
                // Xử lý "Đọc tiếp"
            } else if (id == R.id.nav_favorite_books) {
                // Xử lý "Sách yêu thích"
            } else if (id == R.id.nav_export) {

            } else if (id == R.id.nav_settings) {
                // Xử lý "Cài đặt"
            } else if (id == R.id.nav_about) {
                // Xử lý "About"
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

        // Load dữ liệu
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    public void loadData() {
        // Load Sách của tôi
        myBooksList.clear();
        myBooksList.addAll(bookController.getAllBooks());
        myBooksAdapter.updateData(myBooksList);

        // Load Sách đọc dở
        readingProgressList.clear();
        int userId = 1; // Giả định userId
        List<ReadingProgress> progressList = readingProgressDAO.getReadingProgressByUserId(userId);
        for (ReadingProgress progress : progressList) {
            Book book = bookController.getBookById(progress.getBookId());
            if (book != null) {
                readingProgressList.add(book);
            }
        }
        readingProgressAdapter.updateData(readingProgressList);

        // Load Sách yêu thích
        favoriteBooksList.clear();
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        for (int bookId : favoriteBookIds) {
            Book book = bookController.getBookById(bookId);
            if (book != null) {
                favoriteBooksList.add(book);
            }
        }
        favoriteBooksAdapter.updateData(favoriteBooksList);
    }

    public List<Integer> getFavoriteBookIds() {
        String favoriteIds = sharedPreferences.getString("favorite_book_ids", "");
        if (favoriteIds.isEmpty()) return new ArrayList<>();
        String[] ids = favoriteIds.split(",");
        List<Integer> bookIds = new ArrayList<>();
        for (String id : ids) {
            try {
                bookIds.add(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return bookIds;
    }

    public void addToFavorites(int bookId) {
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (!favoriteBookIds.contains(bookId)) {
            favoriteBookIds.add(bookId);
            saveFavoriteBookIds(favoriteBookIds);
            loadData();
        }
    }

    public void removeFromFavorites(int bookId) {
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (favoriteBookIds.contains(bookId)) {
            favoriteBookIds.remove(Integer.valueOf(bookId));
            saveFavoriteBookIds(favoriteBookIds);
            loadData();
        }
    }

    private void saveFavoriteBookIds(List<Integer> bookIds) {
        String ids = android.text.TextUtils.join(",", bookIds);
        sharedPreferences.edit().putString("favorite_book_ids", ids).apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}