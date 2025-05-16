package com.example.appdocsachv2.controller;

import android.util.Log;

import com.example.appdocsachv2.model.Chapter;
import com.example.appdocsachv2.model.ChapterDAO;

import java.util.ArrayList;
import java.util.List;

public class ChapterController {
    private static final String TAG = "ChapterController";
    private ChapterDAO chapterDAO;

    public ChapterController(ChapterDAO chapterDAO) {
        this.chapterDAO = chapterDAO;
        Log.d(TAG, "ChapterController initialized");
    }

    public long addChapter(Chapter chapter) {
        if (chapter == null || chapter.getTitle() == null || chapter.getTitle().trim().isEmpty() || chapter.getBookId() < 0) {
            Log.w(TAG, "Cannot add chapter: invalid data (chapter=null, title=null/empty, or bookId<0)");
            return -1;
        }
        try {
            long result = chapterDAO.insertChapter(chapter);
            Log.d(TAG, "Added chapter with ID: " + result + " for bookId: " + chapter.getBookId());
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error adding chapter: " + e.getMessage());
            return -1;
        }
    }

    public boolean updateChapter(Chapter chapter) {
        if (chapter == null || chapter.getChapterId() < 0 || chapter.getTitle() == null || chapter.getTitle().trim().isEmpty()) {
            Log.w(TAG, "Cannot update chapter: invalid data (chapter=null, chapterId<0, or title=null/empty)");
            return false;
        }
        try {
            boolean result = chapterDAO.updateChapter(chapter);
            Log.d(TAG, "Updated chapter with ID: " + chapter.getChapterId() + ", Success: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error updating chapter: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteChapter(int chapterId) {
        if (chapterId < 0) {
            Log.w(TAG, "Cannot delete chapter: invalid chapterId: " + chapterId);
            return false;
        }
        try {
            chapterDAO.deleteChaptersByBookId(chapterId);
            Log.d(TAG, "Deleted chapter with ID: " + chapterId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting chapter: " + e.getMessage());
            return false;
        }
    }

    public List<Chapter> getChaptersByBookId(int bookId) {
        if (bookId < 0) {
            Log.w(TAG, "Cannot get chapters: invalid bookId: " + bookId);
            return new ArrayList<>();
        }
        try {
            List<Chapter> chapters = chapterDAO.getChaptersByBookId(bookId);
            Log.d(TAG, "Retrieved chapters for bookId: " + bookId + ", count: " + (chapters != null ? chapters.size() : 0));
            return chapters != null ? chapters : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error getting chapters: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}