package com.example.appdocsachv2.view.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.BookController;
import com.example.appdocsachv2.controller.ChapterController;
import com.example.appdocsachv2.model.Book;
import com.example.appdocsachv2.model.BookDAO;
import com.example.appdocsachv2.model.Chapter;
import com.example.appdocsachv2.model.ChapterDAO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class AddEditBookActivity extends AppCompatActivity {
    private EditText edtTitle, edtAuthor, edtGenre, edtFilePath, edtTotalPages, edtsummary;
    private ImageView imgCover;
    private Button btnSave, btnAddChapter,btnEdit;
    private LinearLayout chapterContainer;
    private TextView tvTitle;
    private BookController bookController;
    private ChapterController chapterController;
    private int bookId = -1;
    private ArrayList<Chapter> chapters = new ArrayList<>();
    private String selectedImagePath = "";
    private String selectedFilePath = "";

    //Xử lý kết quả chọn ảnh, sao chép ảnh vào bộ nhớ ứng dụng và hiển thị bằng Glide
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        String fileExtension = getFileExtension(imageUri);
                        if (fileExtension == null) fileExtension = "jpg";
                        selectedImagePath = copyFileToAppStorage(imageUri, "images", "cover_" + System.currentTimeMillis() + "." + fileExtension);
                        if (selectedImagePath != null) {
                            Glide.with(this).load(selectedImagePath).into(imgCover);
                        }
                    }
                }
            });

    //Xử lý kết quả chọn tệp PDF, sao chép vào bộ nhớ ứng dụng và cập nhật edtFilePath
    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        selectedFilePath = copyFileToAppStorage(fileUri, "pdf", "book_" + System.currentTimeMillis() + ".pdf");
                        if (selectedFilePath != null) {
                            edtFilePath.setText(new File(selectedFilePath).getName());
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);

        // Khởi tạo các view
        edtTitle = findViewById(R.id.edt_tensach);
        edtAuthor = findViewById(R.id.edt_tacgia);
        edtGenre = findViewById(R.id.edt_theloai);
        edtFilePath = findViewById(R.id.edt_linkfile);
        edtTotalPages = findViewById(R.id.edt_sotrang);
        edtsummary = findViewById(R.id.edt_summary);
        btnAddChapter = findViewById(R.id.btnAddChapter);
        chapterContainer = findViewById(R.id.chapterContainer);
        imgCover = findViewById(R.id.btchoosefile);
        btnSave = findViewById(R.id.btsave);
        btnEdit = findViewById(R.id.btedit);
        tvTitle = findViewById(R.id.tvTitle);
        ImageView btnChooseFile = findViewById(R.id.btnchoosefile);

        // Khởi tạo controller
        BookDAO bookDAO = new BookDAO(this);
        bookController = new BookController(bookDAO);
        ChapterDAO chapterDAO = new ChapterDAO(this);
        chapterController = new ChapterController(chapterDAO);

        // Lấy bookId từ Intent
        Intent intent = getIntent();
        bookId = intent.getIntExtra("book_id", -1);
        //Nếu bookId != -1, tải dữ liệu sách và chương để chỉnh sửa
        //Nếu bookId = -1, hiển thị chế độ thêm mới
        if (bookId != -1) {
            loadBookData(bookId);
            loadChaptersData(bookId);
            tvTitle.setText("Sửa sách");
            tvTitle.setTextSize(20);
        } else {
            tvTitle.setText("Thêm sách");
            tvTitle.setTextSize(20);
        }
        if (bookId == -1) {
            // Chế độ thêm mới
            btnSave.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
        } else {
            // Chế độ sửa đổi
            btnSave.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
        }
        // Sự kiện quay lại
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intentBack = new Intent(AddEditBookActivity.this, BookListActivity.class);
            intentBack.putExtra("list_type", "my_books");
            intentBack.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intentBack);
            finish(); // Đảm bảo AddEditBookActivity được xóa khỏi stack
        });

        // Sự kiện chọn ảnh bìa
        imgCover.setOnClickListener(v -> openImagePicker());
        // Sự kiện chọn file
        btnChooseFile.setOnClickListener(v -> openFilePicker());
        // Sự kiện thêm chương
        btnAddChapter.setOnClickListener(v -> addChapterField());
        // Sự kiện lưu
        btnSave.setOnClickListener(v -> saveBookAndChapters());
        btnEdit.setOnClickListener(v -> saveBookAndChapters());

    }
//Mở dialog chọn ảnh từ thiết bị
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
//Mở dialog chọn tệp PDF từ thiết bị
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        filePickerLauncher.launch(intent);
    }
//Lấy phần mở rộng của file từ Uri (ví dụ: jpg, png)
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String fileExtension = null;
        try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    String fileName = cursor.getString(nameIndex);
                    fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileExtension;
    }
//Sao chép file từ Uri vào bộ nhớ ứng dụng
    private String copyFileToAppStorage(Uri uri, String subDir, String fileName) {
        try {
            File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, fileName);
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi sao chép file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
//Thêm trường nhập liệu cho chương mới
    private void addChapterField() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View chapterView = inflater.inflate(R.layout.item_chapter_input, chapterContainer, false);
        EditText edtChapterTitle = chapterView.findViewById(R.id.edt_chapter_title);
        EditText edtStartPage = chapterView.findViewById(R.id.edt_start_page);
        EditText edtEndPage = chapterView.findViewById(R.id.edt_end_page);
        ImageView btnRemove = chapterView.findViewById(R.id.btn_remove_chapter);
        btnRemove.setOnClickListener(v -> chapterContainer.removeView(chapterView));
        chapterContainer.addView(chapterView);
    }
    //Tải dữ liệu sách
    private void loadBookData(int bookId) {
        Book book = bookController.getBookById(bookId);
        if (book != null) {
            edtTitle.setText(book.getTitle());
            edtAuthor.setText(book.getAuthor());
            edtGenre.setText(book.getGenre());
            edtsummary.setText(book.getSummary());
            selectedFilePath = book.getFilePath();
            edtFilePath.setText(new File(book.getFilePath()).getName());
            edtTotalPages.setText(String.valueOf(book.getTotal_pages()));
            selectedImagePath = book.getCoverImage();
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                Glide.with(this).load(selectedImagePath).into(imgCover);
            }
        }
    }

    //Tải dữ liệu sách và chương khi ở chế độ chỉnh sửa
    private void loadChaptersData(int bookId) {
        chapters.clear();
        chapters.addAll(chapterController.getChaptersByBookId(bookId));
        chapterContainer.removeAllViews();
        for (Chapter chapter : chapters) {
            addChapterFieldWithData(chapter);
        }
    }
//tải dữ liệu chương hiện có
    private void addChapterFieldWithData(Chapter chapter) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View chapterView = inflater.inflate(R.layout.item_chapter_input, chapterContainer, false);
        EditText edtChapterTitle = chapterView.findViewById(R.id.edt_chapter_title);
        EditText edtStartPage = chapterView.findViewById(R.id.edt_start_page);
        EditText edtEndPage = chapterView.findViewById(R.id.edt_end_page);
        ImageView btnRemove = chapterView.findViewById(R.id.btn_remove_chapter);
        edtChapterTitle.setText(chapter.getTitle());
        edtStartPage.setText(String.valueOf(chapter.getStartPage()));
        edtEndPage.setText(String.valueOf(chapter.getEndPage()));
        btnRemove.setOnClickListener(v -> {
            int index = chapterContainer.indexOfChild(chapterView);
            if (index >= 0 && index < chapters.size()) {
                chapters.remove(index);
            }
            chapterContainer.removeView(chapterView);
        });
        chapterContainer.addView(chapterView);
    }

    //Lưu thông tin sách và chương vào cơ sở dữ liệu
    private void saveBookAndChapters() {
        String title = edtTitle.getText().toString().trim();
        String author = edtAuthor.getText().toString().trim();
        String genre = edtGenre.getText().toString().trim();
        String summary = edtsummary.getText().toString().trim();
        String filePath = selectedFilePath;
        String totalPagesStr = edtTotalPages.getText().toString().trim();

        if (title.isEmpty() || filePath.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và chọn file PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        if (author.isEmpty() || genre.isEmpty() || summary.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin tác giả, thể loại và tóm tắt", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title.length() > 100 || author.length() > 50 || summary.length() < 10) {
            Toast.makeText(this, "Kiểm tra độ dài tiêu đề, tác giả, hoặc mô tả", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalPages = 0;
        if (!totalPagesStr.isEmpty()) {
            try {
                totalPages = Integer.parseInt(totalPagesStr);
                if (totalPages <= 0) {
                    Toast.makeText(this, "Số trang phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số trang không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        chapters.clear();
        for (int i = 0; i < chapterContainer.getChildCount(); i++) {
            View chapterView = chapterContainer.getChildAt(i);
            EditText edtChapterTitle = chapterView.findViewById(R.id.edt_chapter_title);
            EditText edtStartPage = chapterView.findViewById(R.id.edt_start_page);
            EditText edtEndPage = chapterView.findViewById(R.id.edt_end_page);
            String chapterTitle = edtChapterTitle.getText().toString().trim();
            String startPageStr = edtStartPage.getText().toString().trim();
            String endPageStr = edtEndPage.getText().toString().trim();

            if (!chapterTitle.isEmpty()) {
                if (startPageStr.isEmpty() || endPageStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập trang bắt đầu và kết thúc cho chương " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int startPage = Integer.parseInt(startPageStr);
                    int endPage = Integer.parseInt(endPageStr);
                    if (startPage < 0 || endPage < 0 || startPage >= endPage || endPage > totalPages) {
                        Toast.makeText(this, "Trang không hợp lệ cho chương " + (i + 1), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    chapters.add(new Chapter(bookId, chapterTitle, startPage, endPage));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Trang không hợp lệ cho chương " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Kiểm tra trùng lặp chương
        for (int i = 0; i < chapters.size(); i++) {
            for (int j = i + 1; j < chapters.size(); j++) {
                Chapter chapterI = chapters.get(i);
                Chapter chapterJ = chapters.get(j);
                if ((chapterI.getStartPage() <= chapterJ.getEndPage() && chapterI.getEndPage() >= chapterJ.getStartPage()) ||
                        (chapterJ.getStartPage() <= chapterI.getEndPage() && chapterJ.getEndPage() >= chapterI.getStartPage())) {
                    Toast.makeText(this, "Chương " + (i + 1) + " và " + (j + 1) + " bị trùng lặp", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setFilePath(filePath);
        book.setCoverImage(selectedImagePath);
        book.setTotal_pages(totalPages);
        book.setSummary(summary);

        try {
            if (bookId == -1) {
                bookId = (int) bookController.addBook(book);
                if (bookId == -1) {
                    throw new Exception("Thêm sách thất bại");
                }
            } else {
                book.setBookId(bookId);
                if (!bookController.updateBook(book)) {
                    throw new Exception("Cập nhật sách thất bại");
                }
            }

            chapterController.deleteChapter(bookId);
            for (Chapter chapter : chapters) {
                chapter.setBookId(bookId);
                if (chapterController.addChapter(chapter) == -1) {
                    throw new Exception("Thêm chương thất bại");
                }
            }

            Toast.makeText(this, bookId == -1 ? "Thêm sách và chương thành công" : "Cập nhật sách và chương thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddEditBookActivity.this, BookListActivity.class);
            intent.putExtra("list_type", "my_books");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Đảm bảo AddEditBookActivity được xóa khỏi stack
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}