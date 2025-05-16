package com.example.appdocsachv2.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdocsachv2.R;
import com.example.appdocsachv2.controller.ChapterController;
import com.example.appdocsachv2.model.Chapter;
import com.example.appdocsachv2.model.ChapterDAO;
import com.example.appdocsachv2.view.adapter.ChapterAdapter;
import java.util.ArrayList;
import java.util.List;

public class ChapterListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewChapters;
    private ChapterAdapter chapterAdapter;
    private List<Chapter> chapterList;
    private ChapterController chapterController;
    private int bookId;
    private int selectedChapterPosition = -1; // Theo dõi chương được chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        recyclerViewChapters = findViewById(R.id.recyclerViewChapters);
        ImageButton btnQuaylai = findViewById(R.id.btnQuaylai);
        ImageButton btnBookMark = findViewById(R.id.btnBookMark);
        Button btnDoc = findViewById(R.id.button2);

        // Khởi tạo controller
        ChapterDAO chapterDAO = new ChapterDAO(this);
        chapterController = new ChapterController(chapterDAO);

        chapterList = new ArrayList<>();
        bookId = getIntent().getIntExtra("book_id", -1);
        if (bookId != -1) {
            chapterList.addAll(chapterController.getChaptersByBookId(bookId));
        }

        chapterAdapter = new ChapterAdapter(chapterList, this::onChapterClick);
        recyclerViewChapters.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChapters.setAdapter(chapterAdapter);

        // Sự kiện quay lại
        btnQuaylai.setOnClickListener(v -> onBackPressed());

        // Sự kiện đánh dấu (BookMark) - ví dụ: lưu vị trí chương hiện tại
        btnBookMark.setOnClickListener(v -> {
            if (selectedChapterPosition >= 0 && selectedChapterPosition < chapterList.size()) {
                // Lưu logic bookmark (cần thêm ReadingProgressDAO hoặc cơ chế lưu riêng)
                Toast.makeText(this, "Đã đánh dấu chương: " + chapterList.get(selectedChapterPosition).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện đọc
        btnDoc.setOnClickListener(v -> {
            if (selectedChapterPosition >= 0 && selectedChapterPosition < chapterList.size()) {
                Chapter chapter = chapterList.get(selectedChapterPosition);
                Intent intent = new Intent(ChapterListActivity.this, ReadBookActivity.class);
                intent.putExtra("book_id", bookId);
                intent.putExtra("start_page", chapter.getStartPage());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng chọn một chương", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onChapterClick(Chapter chapter) {
        selectedChapterPosition = chapterList.indexOf(chapter);
        chapterAdapter.notifyDataSetChanged(); // Cập nhật giao diện (có thể thêm highlight)
    }
}