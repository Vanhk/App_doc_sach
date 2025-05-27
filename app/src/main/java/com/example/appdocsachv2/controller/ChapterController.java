package com.example.appdocsachv2.controller;


import com.example.appdocsachv2.model.Chapter;
import com.example.appdocsachv2.model.ChapterDAO;

import java.util.ArrayList;
import java.util.List;

public class ChapterController {
//    private static final String TAG = "ChapterController";
    private ChapterDAO chapterDAO;

    public ChapterController(ChapterDAO chapterDAO) {
        this.chapterDAO = chapterDAO;
    }

    public long addChapter(Chapter chapter) {
        if (chapter == null || chapter.getTitle() == null || chapter.getTitle().trim().isEmpty() || chapter.getBookId() < 0) {
            return -1;
        }
        try {
            long result = chapterDAO.insertChapter(chapter);
            return result;
        } catch (Exception e) {
            return -1;
        }
    }

//    public boolean updateChapter(Chapter chapter) {
//        if (chapter == null || chapter.getChapterId() < 0 || chapter.getTitle() == null || chapter.getTitle().trim().isEmpty()) {
//            return false;
//        }
//        try {
//            boolean result = chapterDAO.updateChapter(chapter);
//            return result;
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public boolean deleteChapter(int chapterId) {
        if (chapterId < 0) {
            return false;
        }
        try {
            chapterDAO.deleteChaptersByBookId(chapterId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Chapter> getChaptersByBookId(int bookId) {
        if (bookId < 0) {
            return new ArrayList<>();
        }
        try {
            List<Chapter> chapters = chapterDAO.getChaptersByBookId(bookId);
            return chapters != null ? chapters : new ArrayList<>();//o	Trả về chapters nếu không null, nếu null hoặc có lỗi, trả về danh sách rỗng
        } catch (Exception e) {
            return new ArrayList<>();//trả về danh sách rỗng để tránh crash
        }
    }
}