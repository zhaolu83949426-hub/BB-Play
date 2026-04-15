package com.bbplay.app.service;

import com.bbplay.app.dto.PictureBookPageCreateRequest;
import com.bbplay.app.dto.PictureBookPageUpdateRequest;
import com.bbplay.app.vo.PictureBookPageVO;

import java.util.List;

/**
 * 绘本页面业务服务
 */
public interface PictureBookPageService {

    List<PictureBookPageVO> listByBookId(Long bookId);

    PictureBookPageVO getById(Long id);

    Long create(Long bookId, PictureBookPageCreateRequest request);

    void update(Long bookId, Long pageId, PictureBookPageUpdateRequest request);

    void delete(Long bookId, Long pageId);

    void reorder(Long bookId, List<Long> pageIds);
}
