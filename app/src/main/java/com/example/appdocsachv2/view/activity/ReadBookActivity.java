package com.example.appdocsachv2.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocsachv2.R;
import com.example.appdocsachv2.model.Chapter;
import com.example.appdocsachv2.model.ChapterDAO;
import com.example.appdocsachv2.model.ReadingProgressDAO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReadBookActivity extends AppCompatActivity {

    private ImageView pdfImageView;
    private TextView tvBookTitle, tvChapter, tvPageNumber;
    private ImageButton btnNext, btnPrev;
    private ScaleGestureDetector scaleGestureDetector;
    private ParcelFileDescriptor fileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;

    private int currentPageIndex = 0;
    private int totalPage = 0;
    private int startPage = 0;
    private int endPage = -1;
    private int userId;

    private String pdfPath;
    private String bookTitle, chapterTitle;
    private int bookId;

    private List<Chapter> chapterList;
    private ReadingProgressDAO readingProgressDAO;
    private float scaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_book);

        // Ánh xạ view
        pdfImageView = findViewById(R.id.pdfView);
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvChapter = findViewById(R.id.tvChapter);
        tvPageNumber = findViewById(R.id.tvPageNumber);
        btnNext = findViewById(R.id.imgbtnext);
        btnPrev = findViewById(R.id.imgbtprev);
        ImageButton btnBack = findViewById(R.id.btnQuaylai);

        // Khởi tạo DAO
        readingProgressDAO = new ReadingProgressDAO(this);
        ChapterDAO chapterDAO = new ChapterDAO(this);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        pdfPath = intent.getStringExtra("pdf_path");
        bookTitle = intent.getStringExtra("book_title");
        chapterTitle = intent.getStringExtra("chapter_title");
        startPage = intent.getIntExtra("start_page", 0);
        endPage = intent.getIntExtra("end_page", -1);
        totalPage = intent.getIntExtra("total_pages", 0);
        bookId = intent.getIntExtra("book_id", -1);
        userId = intent.getIntExtra("user_id", -1);
        currentPageIndex = startPage;


        // Kiểm tra userId
        if (userId == -1) {
            Toast.makeText(this, "Không thể xác định người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Kiểm tra pdfPath
        if (pdfPath == null || pdfPath.isEmpty()) {
            Toast.makeText(this, "Không thể tìm thấy file PDF", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set tiêu đề sách
        tvBookTitle.setText(bookTitle != null ? bookTitle : "Tên sách");

        // Load danh sách chương từ DB
        chapterList = chapterDAO.getChaptersByBookId(bookId);

        // Cập nhật tiêu đề chương
        if (chapterTitle != null && !chapterTitle.isEmpty() && !chapterTitle.equals("Đọc từ đầu")) {
            tvChapter.setText(chapterTitle);
        } else {
            updateChapterByPage(currentPageIndex);
        }

        // Khởi tạo ScaleGestureDetector cho zoom
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f)); // Giới hạn zoom từ 0.1x đến 5x
                pdfImageView.setScaleX(scaleFactor);
                pdfImageView.setScaleY(scaleFactor);
                return true;
            }
        });

        openRenderer();

        // Tăng trang, hiển thị trang mới, lưu tiến trình, cập nhật chương nếu cần
        btnNext.setOnClickListener(view -> {
            int maxPage = (endPage >= 0 && endPage < totalPage) ? endPage : totalPage - 1;
            if (currentPageIndex < maxPage) {
                currentPageIndex++;
                showPage(currentPageIndex);
                saveReadingProgress(currentPageIndex);
                if (chapterTitle == null || chapterTitle.isEmpty() || chapterTitle.equals("Đọc từ đầu")) {
                    updateChapterByPage(currentPageIndex);
                }
            }
        });
//Giảm trang
        btnPrev.setOnClickListener(view -> {
            if (currentPageIndex > startPage) {
                currentPageIndex--;
                showPage(currentPageIndex);
                saveReadingProgress(currentPageIndex);
                if (chapterTitle == null || chapterTitle.isEmpty() || chapterTitle.equals("Đọc từ đầu")) {
                    updateChapterByPage(currentPageIndex);
                }
            }
        });

// Thêm xử lý touch event cho zoom
        pdfImageView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });

        btnBack.setOnClickListener(v -> finish());// đóng activity
    }
    //Mở file pdf và khởi tạo renderer
    private void openRenderer() {
        try {
            File file = new File(pdfPath);
            if (!file.exists()) {
                Toast.makeText(this, "File PDF không tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }

            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(fileDescriptor);
            totalPage = pdfRenderer.getPageCount();

            // Điều chỉnh startPage và endPage dựa trên totalPage
            if (startPage < 0 || startPage >= totalPage) startPage = 0;
            if (endPage >= totalPage || endPage == -1) endPage = totalPage - 1;



            showPage(currentPageIndex);
            // Cập nhật chương ngay khi mở nếu không có chapterTitle cụ thể
            if (chapterTitle == null || chapterTitle.isEmpty() || chapterTitle.equals("Đọc từ đầu")) {
                updateChapterByPage(currentPageIndex);
            }

            // Lưu tiến trình đọc ban đầu
            saveReadingProgress(currentPageIndex);

        } catch (IOException e) {
            Toast.makeText(this, "Không thể mở file PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    //Hiển thị một trang PDF tại chỉ số index
    private void showPage(int index) {
        if (pdfRenderer == null || index < 0 || index >= totalPage) return;

        if (currentPage != null) {
            currentPage.close();
        }

        currentPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(
                currentPage.getWidth() * 2, // Tăng kích thước để rõ hơn
                currentPage.getHeight() * 2,
                Bitmap.Config.ARGB_8888
        );
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        pdfImageView.setImageBitmap(bitmap);
        tvPageNumber.setText((index + 1) + " / " + totalPage);
        pdfImageView.setScaleX(scaleFactor); // Áp dụng scale sau khi render
        pdfImageView.setScaleY(scaleFactor);
    }
    //Cập nhật tiêu đề chương dựa trên trang hiện tại
    private void updateChapterByPage(int pageIndex) {
        if (chapterList == null || chapterList.isEmpty()) {
            tvChapter.setText("Không có chương");
            return;
        }

        for (Chapter chapter : chapterList) {
            if (pageIndex >= chapter.getStartPage() && pageIndex <= chapter.getEndPage()) {
                tvChapter.setText(chapter.getTitle());
                return;
            }
        }
        tvChapter.setText("Chưa thuộc chương nào");
    }
    //lưu trang hiện tại vào csdl
    private void saveReadingProgress(int pageIndex) {
        if (bookId == -1 || userId == -1) {
            return;
        }

        readingProgressDAO.insertOrUpdateReadingProgress(userId, bookId, pageIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (currentPage != null) currentPage.close();
            if (pdfRenderer != null) pdfRenderer.close();
            if (fileDescriptor != null) fileDescriptor.close();
        } catch (IOException e) {
        }
    }
}