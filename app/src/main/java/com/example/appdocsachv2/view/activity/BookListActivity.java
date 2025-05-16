package com.example.appdocsachv2.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    private String listType;
    private String selectedAuthor = "All";
    private String selectedGenre = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        recyclerViewBooks = findViewById(R.id.recyclerBookList);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnAddBook = findViewById(R.id.btnAddBook);
        spinnerAuthor = findViewById(R.id.spinnerauthor);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        tvTitle = findViewById(R.id.tvTitle);

        // Khởi tạo controller
        BookDAO bookDAO = new BookDAO(this);
        bookController = new BookController(bookDAO);
        readingProgressDAO = new ReadingProgressDAO(this);

        bookList = new ArrayList<>();
        filteredBookList = new ArrayList<>();
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerViewBooks.setLayoutManager(layoutManager);

        // Lấy loại danh sách từ Intent
        listType = getIntent().getStringExtra("list_type");
        if (listType == null) listType = "my_books";
        Log.d(TAG, "List type: " + listType);

        // Xác định xem có hiển thị icon yêu thích hay không
        boolean showFavoriteIcon = !listType.equals("my_books");

        bookAdapter = new BookAdapter(filteredBookList, book -> {
            Intent intent = new Intent(BookListActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookId());
            startActivity(intent);
        }, this, showFavoriteIcon);
        recyclerViewBooks.setAdapter(bookAdapter);

        // Cập nhật tiêu đề và ẩn nút thêm sách nếu không phải "Sách của tôi"
        if (listType.equals("reading_progress")) {
            tvTitle.setText("Đọc tiếp");
            btnAddBook.setVisibility(View.GONE);
        } else if (listType.equals("favorite_books")) {
            tvTitle.setText("Sách yêu thích");
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

        // Thiết lập Spinner cho Genre (lấy từ cơ sở dữ liệu)
        List<String> genres = getUniqueGenres();
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genres);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

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

        btnBack.setOnClickListener(v -> onBackPressed());
        btnAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(BookListActivity.this, AddEditBookActivity.class);
            intent.putExtra("book_id", -1);
            startActivity(intent);
        });

        loadBooks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
    }

    public void loadBooks() {
        bookList.clear();
        if (listType.equals("reading_progress")) {
            int userId = 1; // Giả định userId
            List<ReadingProgress> progressList = readingProgressDAO.getReadingProgressByUserId(userId);
            Log.d(TAG, "Reading progress list size: " + (progressList != null ? progressList.size() : 0));
            for (ReadingProgress progress : progressList) {
                Book book = bookController.getBookById(progress.getBookId());
                if (book != null) {
                    bookList.add(book);
                }
            }
        } else if (listType.equals("favorite_books")) {
            List<Integer> favoriteBookIds = getFavoriteBookIds();
            Log.d(TAG, "Favorite book IDs size: " + (favoriteBookIds != null ? favoriteBookIds.size() : 0));
            for (int bookId : favoriteBookIds) {
                Book book = bookController.getBookById(bookId);
                if (book != null) {
                    bookList.add(book);
                }
            }
        } else {
            bookList.addAll(bookController.getAllBooks());
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

        filterBooks();
    }

    private List<String> getUniqueAuthors() {
        Set<String> authorsSet = new HashSet<>();
        authorsSet.add("All"); // Thêm tùy chọn "All"
        for (Book book : bookList) {
            if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
                authorsSet.add(book.getAuthor());
            }
        }
        List<String> authors = new ArrayList<>(authorsSet);
        Collections.sort(authors); // Sắp xếp theo thứ tự bảng chữ cái
        return authors;
    }

    private List<String> getUniqueGenres() {
        Set<String> genresSet = new HashSet<>();
        genresSet.add("All"); // Thêm tùy chọn "All"
        for (Book book : bookController.getAllBooks()) { // Lấy từ tất cả sách trong cơ sở dữ liệu
            if (book.getGenre() != null && !book.getGenre().isEmpty()) {
                genresSet.add(book.getGenre());
            }
        }
        List<String> genres = new ArrayList<>(genresSet);
        Collections.sort(genres); // Sắp xếp theo thứ tự bảng chữ cái
        return genres;
    }

    private void filterBooks() {
        filteredBookList.clear();
        for (Book book : bookList) {
            boolean matchesAuthor = selectedAuthor.equals("All") || (book.getAuthor() != null && book.getAuthor().equals(selectedAuthor));
            boolean matchesGenre = selectedGenre.equals("All") || (book.getGenre() != null && book.getGenre().equals(selectedGenre));
            if (matchesAuthor && matchesGenre) {
                filteredBookList.add(book);
            }
        }
        Log.d(TAG, "Filtered book list size: " + filteredBookList.size());
        bookAdapter.updateData(filteredBookList);
    }

    public List<Integer> getFavoriteBookIds() {
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);
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
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (!favoriteBookIds.contains(bookId)) {
            favoriteBookIds.add(bookId);
            saveFavoriteBookIds(favoriteBookIds);
            loadBooks();
        }
    }

    public void removeFromFavorites(int bookId) {
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);
        List<Integer> favoriteBookIds = getFavoriteBookIds();
        if (favoriteBookIds.contains(bookId)) {
            favoriteBookIds.remove(Integer.valueOf(bookId));
            saveFavoriteBookIds(favoriteBookIds);
            loadBooks();
        }
    }

    private void saveFavoriteBookIds(List<Integer> bookIds) {
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);
        String ids = android.text.TextUtils.join(",", bookIds);
        sharedPreferences.edit().putString("favorite_book_ids", ids).apply();
    }
}