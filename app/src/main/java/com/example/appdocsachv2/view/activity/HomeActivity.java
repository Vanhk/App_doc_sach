package com.example.appdocsachv2.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
//    private static final String TAG = "HomeActivity";
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private RecyclerView recyclerMyBooks, recyclerReadingProgress, recyclerMyFavoriteBooks;
    private BookAdapter myBooksAdapter, readingProgressAdapter, favoriteBooksAdapter;
    private List<Book> myBooksList, filteredMyBooksList, readingProgressList, favoriteBooksList;
    private BookController bookController;
    private ReadingProgressDAO readingProgressDAO;
    private NavigationView navigationView;
    private SharedPreferences sharedPreferences;
    private int userId;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private AutoCompleteTextView searchBar;
    private List<String> suggestionList;
    private LinearLayout genreFilterContainer;
    private String selectedGenre = "All"; // Thể loại đang được chọn
    private List<Button> genreButtons = new ArrayList<>(); // Lưu danh sách các nút thể loại

    private BroadcastReceiver favoriteChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadData(); // Tải lại dữ liệu để cập nhật danh sách yêu thích
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);
        //kiểm tra đăng nhập
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        userId = sessionManager.getUserId();

        setContentView(R.layout.activity_home);

        //Đăng ký favoriteChangeReceiver để lắng nghe sự kiện thay đổi danh sách yêu thích từ ChapterListActivity
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
        genreFilterContainer = findViewById(R.id.genreFilterContainer);

        // Khởi tạo controller và DAO
        BookDAO bookDAO = new BookDAO(this);
        bookController = new BookController(bookDAO);
        readingProgressDAO = new ReadingProgressDAO(this);
        sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);

        // Khởi tạo danh sách
        myBooksList = new ArrayList<>();
        filteredMyBooksList = new ArrayList<>();
        readingProgressList = new ArrayList<>();
        favoriteBooksList = new ArrayList<>();
        suggestionList = new ArrayList<>();
        updateSuggestions();
        ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestionList);
        searchBar.setAdapter(suggestionAdapter);

        // Cấu hình RecyclerView
        //Thiết lập LinearLayoutManager với hướng ngang (HORIZONTAL) cho 3 RecyclerView
        //Danh sách dữ liệu tương ứng (filteredMyBooksList, readingProgressList, favoriteBooksList)
        LinearLayoutManager myBooksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerMyBooks.setLayoutManager(myBooksLayoutManager);
        myBooksAdapter = new BookAdapter(filteredMyBooksList, book -> {
            Intent intent = new Intent(HomeActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            intent.putExtra("list_type", "my_books");
            intent.putExtra("fromHome", true);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        }, this, false, bookController, userId);
        recyclerMyBooks.setAdapter(myBooksAdapter);

        LinearLayoutManager readingProgressLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerReadingProgress.setLayoutManager(readingProgressLayoutManager);
        readingProgressAdapter = new BookAdapter(readingProgressList, book -> {
            Intent intent = new Intent(HomeActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            intent.putExtra("list_type", "reading_progress");
            intent.putExtra("fromHome", true);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        }, this, true, bookController, userId);
        recyclerReadingProgress.setAdapter(readingProgressAdapter);

        LinearLayoutManager favoriteBooksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerMyFavoriteBooks.setLayoutManager(favoriteBooksLayoutManager);
        favoriteBooksAdapter = new BookAdapter(favoriteBooksList, book -> {
            Intent intent = new Intent(HomeActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            intent.putExtra("list_type", "favorite_books");
            intent.putExtra("fromHome", true);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        }, this, true, bookController, userId);
        recyclerMyFavoriteBooks.setAdapter(favoriteBooksAdapter);

        // Xử lý tìm kiếm trong searchBar
        //hiển thị kết quả trong recycler view my book
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
                filteredMyBooksList.clear();
                filteredMyBooksList.addAll(filteredBooks);
                myBooksAdapter.updateData(filteredMyBooksList);
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
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });

        // Sự kiện nút Thêm sách
        //chuyển đến AddEditBookActivity với book_id = -1 (thêm sách mới)
        //sử dụng activityResultLauncher để xử lý kết quả trả về
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
        //Chuyển đến BookListActivity với các list_type tương ứng (my_books, reading_progress, favorite_books)
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

        // Khởi tạo DrawerLayout và NavigationView để tạo menu điều hướng bên trái
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        //Cấu hình ActionBarDrawerToggle để hiển thị nút hamburger trên Toolbar
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
                sessionManager.logout();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_settings) {
            } else if (id == R.id.nav_about) {
                StringBuilder aboutMessage = new StringBuilder();
                aboutMessage.append("ĐẠI HỌC CÔNG NGHIỆP HÀ NỘI\n");
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

        // Xử lý nút Back khi muốn ra khỏi menu
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
                        loadData();
                    }
                });

        // Load dữ liệu và thiết lập bộ lọc thể loại
        loadData();
        setupGenreFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        setupGenreFilters();
    }

    public void loadData() {
        // Load Sách của tôi
        myBooksList.clear();
        List<Book> allBooks = bookController.getAllBooks();
        if (allBooks != null) {
            myBooksList.addAll(allBooks);
        }
        // Load Sách đọc dở
        readingProgressList.clear();
        List<ReadingProgress> progressList = readingProgressDAO.getReadingProgress(userId);
        if (progressList != null) {
            for (ReadingProgress progress : progressList) {
                Book book = bookController.getBookById(progress.getBookId());
                if (book != null) {
                    readingProgressList.add(book);
                }
            }
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
        }
        favoriteBooksAdapter.updateData(favoriteBooksList);
        // Cập nhật danh sách gợi ý tìm kiếm
        updateSuggestions();
        ArrayAdapter<String> suggestionAdapter = (ArrayAdapter<String>) searchBar.getAdapter();
        suggestionAdapter.clear();
        suggestionAdapter.addAll(suggestionList);
        suggestionAdapter.notifyDataSetChanged();
        filterBooksByGenre();// Lọc sách theo thể loại
    }
// xử lý chọn theer loại ở my book
    private void setupGenreFilters() {
        // Lấy danh sách thể loại duy nhất từ bảng Book
        Set<String> genresSet = new HashSet<>();
        genresSet.add("All");
        for (Book book : myBooksList) {
            if (book.getGenre() != null && !book.getGenre().isEmpty()) {
                genresSet.add(book.getGenre());
            }
        }
        List<String> genres = new ArrayList<>(genresSet);
        Collections.sort(genres);

        // Xóa các nút cũ trong container
        genreFilterContainer.removeAllViews();
        genreButtons.clear();

        // Tạo các nút Button cho từng thể loại, thêm vào genreFilterContainer
        for (String genre : genres) {
            Button genreButton = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 24, 0);
            genreButton.setLayoutParams(params);
            genreButton.setText(genre);
            genreButton.setTextSize(15);
            genreButton.setTextColor(getResources().getColor(android.R.color.white));
            genreButton.setAllCaps(false);
            genreButton.setPadding(32, 16, 32, 16); // Tăng padding để nút dài ra theo text
            genreButton.setBackgroundResource(R.drawable.ripple_effect); // Sử dụng ripple effect
            // Đặt màu nền ban đầu
            genreButton.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.blue));
            // Nếu thể loại này đang được chọn, làm nổi bật nó
            if (genre.equals(selectedGenre)) {
                genreButton.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.blue_dam));
            }
            // Khi nhấn nút, cập nhật selectedGenre và gọi filterBooksByGenre() để lọc sách
            genreButton.setOnClickListener(v -> {
                selectedGenre = genre;
                // Cập nhật màu nền của tất cả các nút
                for (Button btn : genreButtons) {
                    btn.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.blue));
                }
                genreButton.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.blue_dam));
                filterBooksByGenre();
            });

            genreButtons.add(genreButton);
            genreFilterContainer.addView(genreButton);
        }
    }
//xử lý lấy danh sách thể loại tương ứng
    private void filterBooksByGenre() {
        filteredMyBooksList.clear();
        String keyword = searchBar.getText().toString().trim().toLowerCase();
        for (Book book : myBooksList) {
            boolean matchesGenre = selectedGenre.equals("All") || (book.getGenre() != null && book.getGenre().equals(selectedGenre));
            boolean matchesSearch = keyword.isEmpty() ||
                    book.getTitle().toLowerCase().contains(keyword) ||
                    (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(keyword)) ||
                    (book.getGenre() != null && book.getGenre().toLowerCase().contains(keyword));
            if (matchesGenre && matchesSearch) {
                filteredMyBooksList.add(book);
            }
        }
        myBooksAdapter.updateData(filteredMyBooksList);
    }
//tạo danh sách gợi ý tìm kiếm dựa trên thông tin sách
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
    }
//Lấy danh sách ID của sách yêu thích từ SharedPreferences
    public List<Integer> getFavoriteBookIds() {
        String favoriteIds = sharedPreferences.getString("favorite_book_ids_user_" + userId, "");
        if (favoriteIds.isEmpty()) {
            return new ArrayList<>();
        }
        String[] ids = favoriteIds.split(",");
        List<Integer> bookIds = new ArrayList<>();
        for (String id : ids) {
            try {
                bookIds.add(Integer.parseInt(id.trim()));
            } catch (NumberFormatException e) {
            }
        }
        return bookIds;
    }
//thêm 1 sách vào ds yêu thích
    public void addToFavorites(int bookId) {
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (!favoriteBookIds.contains(bookId)) {
            favoriteBookIds.add(bookId);
            saveFavoriteBookIds(favoriteBookIds);
            loadData();
        }
    }
    //xóa 1 sách khỏi ds yêu thích
    public void removeFromFavorites(int bookId) {
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (favoriteBookIds.contains(bookId)) {
            favoriteBookIds.remove(Integer.valueOf(bookId));
            saveFavoriteBookIds(favoriteBookIds);
            loadData();
        }
    }
//Lưu danh sách ID sách yêu thích vào SharedPreferences
    private void saveFavoriteBookIds(List<Integer> bookIds) {
        String ids = android.text.TextUtils.join(",", bookIds);
        sharedPreferences.edit().putString("favorite_book_ids_user_" + userId, ids).apply();
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