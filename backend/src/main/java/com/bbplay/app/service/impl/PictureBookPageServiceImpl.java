package com.bbplay.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bbplay.app.dto.PictureBookPageCreateRequest;
import com.bbplay.app.dto.PictureBookPageUpdateRequest;
import com.bbplay.app.entity.PictureBook;
import com.bbplay.app.entity.PictureBookPage;
import com.bbplay.app.exception.BusinessException;
import com.bbplay.app.mapper.PictureBookMapper;
import com.bbplay.app.mapper.PictureBookPageMapper;
import com.bbplay.app.service.PictureBookPageService;
import com.bbplay.app.vo.PictureBookPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 绘本页面业务实现
 */
@Service
@RequiredArgsConstructor
public class PictureBookPageServiceImpl implements PictureBookPageService {

    private final PictureBookPageMapper pictureBookPageMapper;
    private final PictureBookMapper pictureBookMapper;

    @Override
    public List<PictureBookPageVO> listByBookId(Long bookId) {
        LambdaQueryWrapper<PictureBookPage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PictureBookPage::getBookId, bookId).orderByAsc(PictureBookPage::getPageNumber);
        return pictureBookPageMapper.selectList(wrapper).stream()
            .map(PictureBookConverter::toPageVO)
            .toList();
    }

    @Override
    public PictureBookPageVO getById(Long id) {
        PictureBookPage page = requirePage(id);
        return PictureBookConverter.toPageVO(page);
    }

    @Override
    @Transactional
    public Long create(Long bookId, PictureBookPageCreateRequest request) {
        requireBook(bookId);
        PictureBookPage page = new PictureBookPage();
        page.setBookId(bookId);
        fillPage(request, page);
        LocalDateTime now = LocalDateTime.now();
        page.setCreatedAt(now);
        page.setUpdatedAt(now);
        pictureBookPageMapper.insert(page);
        updateBookPageCount(bookId);
        return page.getId();
    }

    @Override
    @Transactional
    public void update(Long bookId, Long pageId, PictureBookPageUpdateRequest request) {
        PictureBookPage existing = requirePageInBook(bookId, pageId);
        fillPage(request, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        pictureBookPageMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void delete(Long bookId, Long pageId) {
        PictureBookPage page = requirePageInBook(bookId, pageId);
        pictureBookPageMapper.deleteById(pageId);
        updateBookPageCount(page.getBookId());
    }

    @Override
    @Transactional
    public void reorder(Long bookId, List<Long> pageIds) {
        requireBook(bookId);
        for (int i = 0; i < pageIds.size(); i++) {
            Long pageId = pageIds.get(i);
            LambdaUpdateWrapper<PictureBookPage> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PictureBookPage::getId, pageId)
                .eq(PictureBookPage::getBookId, bookId)
                .set(PictureBookPage::getPageNumber, i + 1)
                .set(PictureBookPage::getUpdatedAt, LocalDateTime.now());
            pictureBookPageMapper.update(null, wrapper);
        }
    }

    private void updateBookPageCount(Long bookId) {
        LambdaQueryWrapper<PictureBookPage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PictureBookPage::getBookId, bookId);
        Long count = pictureBookPageMapper.selectCount(wrapper);
        LambdaUpdateWrapper<PictureBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PictureBook::getId, bookId)
            .set(PictureBook::getPageCount, count.intValue())
            .set(PictureBook::getUpdatedAt, LocalDateTime.now());
        pictureBookMapper.update(null, updateWrapper);
    }

    private PictureBookPage requirePage(Long id) {
        PictureBookPage page = pictureBookPageMapper.selectById(id);
        if (page == null) {
            throw new BusinessException("页面不存在");
        }
        return page;
    }

    /**
     * 校验页面归属，避免跨绘本误操作
     */
    private PictureBookPage requirePageInBook(Long bookId, Long pageId) {
        PictureBookPage page = requirePage(pageId);
        if (!bookId.equals(page.getBookId())) {
            throw new BusinessException("页面不属于当前绘本");
        }
        return page;
    }

    private void requireBook(Long bookId) {
        PictureBook book = pictureBookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("绘本不存在");
        }
    }

    private void fillPage(PictureBookPageCreateRequest request, PictureBookPage page) {
        page.setPageNumber(request.getPageNumber());
        page.setImageUrl(request.getImageUrl());
        page.setTextContent(request.getTextContent());
        page.setAudioUrl(request.getAudioUrl());
        page.setDurationSec(request.getDurationSec());
    }

    private void fillPage(PictureBookPageUpdateRequest request, PictureBookPage page) {
        page.setPageNumber(request.getPageNumber());
        page.setImageUrl(request.getImageUrl());
        page.setTextContent(request.getTextContent());
        page.setAudioUrl(request.getAudioUrl());
        page.setDurationSec(request.getDurationSec());
    }
}
