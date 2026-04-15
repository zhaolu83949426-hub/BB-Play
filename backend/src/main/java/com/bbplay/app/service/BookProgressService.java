package com.bbplay.app.service;

import com.bbplay.app.dto.BookProgressItem;
import com.bbplay.app.dto.BookProgressUpdateRequest;

/**
 * 绘本阅读进度服务
 */
public interface BookProgressService {

    BookProgressItem getProgress(String uid, Long bookId);

    void updateProgress(String uid, Long bookId, BookProgressUpdateRequest request);
}
