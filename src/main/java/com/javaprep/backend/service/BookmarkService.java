package com.javaprep.backend.service;

import com.javaprep.backend.dto.bookmark.BookmarkResponse;
import com.javaprep.backend.dto.question.QuestionResponse;

import java.util.List;

public interface BookmarkService {
    BookmarkResponse addBookmark(String userId, String questionId);
    void removeBookmark(String userId, String questionId);
    List<BookmarkResponse> listBookmarks(String userId);
    List<QuestionResponse> getBookmarkedQuestionsDetails(String userId);
}
