package com.example.appdocsachv2.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.BookController;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;
import com.example.appdocsachv2.model.ReadingProgress;
import com.example.appdocsachv2.model.ReadingProgressDAO;
import com.example.appdocsachv2.utils.SessionManager;
import com.example.appdocsachv2.view.adapter.BookAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookListActivity extends AppCompatActivity {
    private static final String TAG = "BookListActivity";
    private RecyclerView recyclerViewBooks;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private List<Book> filteredBookList;
    private BookController bookController;
    private ReadingProgressDAO readingProgressDAO;
    private TextView tvTitle;
    private ImageView btnAddBook;
    private Spinner spinnerAuthor, spinnerGenre;
    private AutoCompleteTextView searchBar;
    private String listType = "my_books"; // Gán giá trị mặc định
    private String selectedAuthor = "All";
    private String selectedGenre = "All";
    private boolean fromSearch;
    private String searchKeyword;
    private int userId;
    private SessionManager sessionManager;
    private List<String> suggestionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);
        userId = getIntent().getIntExtra("user_id", sessionManager.getUserId());
        if (userId == -1) {
            Log.e(TAG, "User ID not found, redirecting to LoginActivity");
            Intent intent = new Intent(BookListActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        Log.d(TAG, "User ID retrieved: " + userId);

        recyclerViewBooks = findViewById(R.id.recyclerBookList);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnAddBook = findViewById(R.id.btnAddBook);
        spinnerAuthor = findViewById(R.id.spinnerauthor);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        tvTitle = findViewById(R.id.tvTitle);
        searchBar = findViewById(R.id.search_bar);

        // Khởi tạo controller
        BookDAO bookDAO = new BookDAO(this);
        bookController = new BookController(bookDAO);
        readingProgressDAO = new ReadingProgressDAO(this);

        bookList = new ArrayList<>();
        filteredBookList = new ArrayList<>();
        suggestionList = new ArrayList<>();

        // Thiết lập GridLayoutManager với 3 cột
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerViewBooks.setLayoutManager(gridLayoutManager);

        // Lấy loại danh sách và thông tin tìm kiếm từ Intent
        listType = getIntent().getStringExtra("list_type");
        if (listType == null) listType = "my_books"; // Đảm bảo listType không null
        fromSearch = getIntent().getBooleanExtra("fromSearch", false);
        searchKeyword = getIntent().getStringExtra("search_keyword");
        Log.d(TAG, "List type: " + listType + ", fromSearch: " + fromSearch + ", searchKeyword: " + searchKeyword);

        // Xác định xem có hiển thị icon yêu thích hay không
        boolean showFavoriteIcon = !listType.equals("my_books");
        bookAdapter = new BookAdapter(filteredBookList, book -> {
            Intent intent = new Intent(BookListActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            intent.putExtra("list_type", listType);
            intent.putExtra("fromHome", false);
            intent.putExtra("user_id", userId);
            Log.d(TAG, "Navigating to BookDetailActivity with book_id: " + book.getBookId() + ", listType: " + listType + ", fromHome: false");
            startActivity(intent);
        }, this, showFavoriteIcon, bookController, userId);
        recyclerViewBooks.setAdapter(bookAdapter);

        // Cập nhật tiêu đề và nút thêm sách
        if (listType.equals("reading_progress")) {
            tvTitle.setText("Đọc tiếp");
            btnAddBook.setVisibility(View.GONE);
        } else if (listType.equals("favorite_books")) {
            tvTitle.setText("Sách yêu thích");
            btnAddBook.setVisibility(View.GONE);
        } else if (fromSearch) {
            tvTitle.setText("Kết quả tìm kiếm");
            btnAddBook.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Quản lý sách");
            btnAddBook.setVisibility(View.VISIBLE);
        }

        // Thiết lập Spinner cho Author
        List<String> authors = getUniqueAuthors();
        ArrayAdapter<String> authorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, authors);
        authorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAuthor.setAdapter(authorAdapter);

        // Thiết lập Spinner cho Genre
        List<String> genres = getUniqueGenres();
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genres);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

        // Điều chỉnh giao diện dựa trên fromSearch
        if (fromSearch) {
            searchBar.setVisibility(View.VISIBLE);
            spinnerAuthor.setVisibility(View.GONE);
            spinnerGenre.setVisibility(View.GONE);
            searchBar.setText(searchKeyword != null ? searchKeyword : "");
        } else {
            searchBar.setVisibility(View.GONE);
            spinnerAuthor.setVisibility(View.VISIBLE);
            spinnerGenre.setVisibility(View.VISIBLE);
        }

        // Thiết lập gợi ý cho searchBar
        updateSuggestions();
        ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestionList);
        searchBar.setAdapter(suggestionAdapter);

        // Xử lý tìm kiếm trong searchBar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                filterBooks(keyword);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý khi chọn gợi ý
        searchBar.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSuggestion = (String) parent.getItemAtPosition(position);
            searchBar.setText(selectedSuggestion);
            filterBooks(selectedSuggestion);
        });

        // Xử lý sự kiện chọn Author
        spinnerAuthor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAuthor = authors.get(position);
                Log.d(TAG, "Selected author: " + selectedAuthor);
                filterBooks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedAuthor = "All";
                filterBooks();
            }
        });

        // Xử lý sự kiện chọn Genre
        spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGenre = genres.get(position);
                Log.d(TAG, "Selected genre: " + selectedGenre);
                filterBooks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGenre = "All";
                filterBooks();
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(BookListActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        });

        btnAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(BookListActivity.this, AddEditBookActivity.class);
            intent.putExtra("book_id", -1);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        loadBooks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called, reloading books");
        loadBooks();
    }

    public void loadBooks() {
        bookList.clear();
        if (listType.equals("reading_progress")) {
            List<ReadingProgress> progressList = readingProgressDAO.getReadingProgress(userId);
            Log.d(TAG, "Fetched " + (progressList != null ? progressList.size() : 0) + " reading progress records for userId: " + userId);
            if (progressList != null) {
                for (ReadingProgress progress : progressList) {
                    Book book = bookController.getBookById(progress.getBookId());
                    if (book != null) {
                        bookList.add(book);
                        Log.d(TAG, "Added book to reading progress: " + book.getTitle());
                    } else {
                        Log.e(TAG, "Book not found for progress with bookId: " + progress.getBookId());
                    }
                }
            }
            Log.d(TAG, "Loaded " + bookList.size() + " reading progress books");
        } else if (listType.equals("favorite_books")) {
            List<Integer> favoriteBookIds = getFavoriteBookIds();
            Log.d(TAG, "Favorite book IDs size: " + (favoriteBookIds != null ? favoriteBookIds.size() : 0));
            if (favoriteBookIds != null) {
                for (int bookId : favoriteBookIds) {
                    Book book = bookController.getBookById(bookId);
                    if (book != null) {
                        bookList.add(book);
                    }
                }
            }
        } else {
            List<Book> allBooks = bookController.getAllBooks();
            if (allBooks != null) {
                bookList.addAll(allBooks);
            }
        }
        Log.d(TAG, "Loaded book list size: " + bookList.size());

        // Cập nhật lại danh sách author và genre khi load sách mới
        ArrayAdapter<String> authorAdapter = (ArrayAdapter<String>) spinnerAuthor.getAdapter();
        authorAdapter.clear();
        List<String> authors = getUniqueAuthors();
        authorAdapter.addAll(authors);
        authorAdapter.notifyDataSetChanged();
        Log.d(TAG, "Authors list size: " + authors.size());

        ArrayAdapter<String> genreAdapter = (ArrayAdapter<String>) spinnerGenre.getAdapter();
        genreAdapter.clear();
        List<String> genres = getUniqueGenres();
        genreAdapter.addAll(genres);
        genreAdapter.notifyDataSetChanged();
        Log.d(TAG, "Genres list size: " + genres.size());

        // Đặt lại selectedAuthor và selectedGenre nếu không còn trong danh sách
        if (!authors.contains(selectedAuthor)) {
            selectedAuthor = "All";
            spinnerAuthor.setSelection(0);
        }
        if (!genres.contains(selectedGenre)) {
            selectedGenre = "All";
            spinnerGenre.setSelection(0);
        }

        // Cập nhật danh sách gợi ý
        updateSuggestions();
        ArrayAdapter<String> suggestionAdapter = (ArrayAdapter<String>) searchBar.getAdapter();
        suggestionAdapter.clear();
        suggestionAdapter.addAll(suggestionList);
        suggestionAdapter.notifyDataSetChanged();

        filterBooks();
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

    private List<String> getUniqueAuthors() {
        Set<String> authorsSet = new HashSet<>();
        authorsSet.add("All");
        for (Book book : bookList) {
            if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
                authorsSet.add(book.getAuthor());
            }
        }
        List<String> authors = new ArrayList<>(authorsSet);
        Collections.sort(authors);
        return authors;
    }

    private List<String> getUniqueGenres() {
        Set<String> genresSet = new HashSet<>();
        genresSet.add("All");
        List<Book> allBooks = bookController.getAllBooks();
        if (allBooks != null) {
            for (Book book : allBooks) {
                if (book.getGenre() != null && !book.getGenre().isEmpty()) {
                    genresSet.add(book.getGenre());
                }
            }
        }
        List<String> genres = new ArrayList<>(genresSet);
        Collections.sort(genres);
        return genres;
    }

    private void filterBooks() {
        filterBooks(searchBar.getText().toString().trim());
    }

    private void filterBooks(String searchQuery) {
        filteredBookList.clear();
        for (Book book : bookList) {
            boolean matchesAuthor = selectedAuthor.equals("All") || (book.getAuthor() != null && book.getAuthor().equals(selectedAuthor));
            boolean matchesGenre = selectedGenre.equals("All") || (book.getGenre() != null && book.getGenre().equals(selectedGenre));
            boolean matchesSearch = searchQuery.isEmpty() ||
                    (book.getTitle() != null && book.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (book.getGenre() != null && book.getGenre().toLowerCase().contains(searchQuery.toLowerCase()));
            if (matchesAuthor && matchesGenre && matchesSearch) {
                filteredBookList.add(book);
            }
        }
        Log.d(TAG, "Filtered book list size: " + filteredBookList.size());
        bookAdapter.updateData(filteredBookList);
    }

    public List<Integer> getFavoriteBookIds() {
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);
        String favoriteIds = sharedPreferences.getString("favorite_book_ids_user_" + userId, "");
        Log.d(TAG, "Fetched favorite IDs for user " + userId + ": " + favoriteIds);
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
        Log.d(TAG, "Adding bookId " + bookId + " to favorites for user " + userId);
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (!favoriteBookIds.contains(bookId)) {
            favoriteBookIds.add(bookId);
            saveFavoriteBookIds(favoriteBookIds);
            loadBooks();
            Log.d(TAG, "Added bookId " + bookId + " to favorites");
        } else {
            Log.d(TAG, "BookId " + bookId + " already in favorites");
        }
    }

    public void removeFromFavorites(int bookId) {
        Log.d(TAG, "Removing bookId " + bookId + " from favorites for user " + userId);
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (favoriteBookIds.contains(bookId)) {
            favoriteBookIds.remove(Integer.valueOf(bookId));
            saveFavoriteBookIds(favoriteBookIds);
            loadBooks();
            Log.d(TAG, "Removed bookId " + bookId + " from favorites");
        } else {
            Log.d(TAG, "BookId " + bookId + " not found in favorites");
        }
    }

    private void saveFavoriteBookIds(List<Integer> bookIds) {
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);
        String ids = android.text.TextUtils.join(",", bookIds);
        sharedPreferences.edit().putString("favorite_book_ids_user_" + userId, ids).apply();
        Log.d(TAG, "Saved favorite book IDs for user " + userId + ": " + ids);
    }
}