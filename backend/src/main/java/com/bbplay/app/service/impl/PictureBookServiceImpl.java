package com.bbplay.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.PictureBookCreateRequest;
import com.bbplay.app.dto.PictureBookUpdateRequest;
import com.bbplay.app.dto.picturebook.AdminPictureBookListQuery;
import com.bbplay.app.dto.picturebook.PictureBookListQuery;
import com.bbplay.app.entity.PictureBook;
import com.bbplay.app.entity.PictureBookPage;
import com.bbplay.app.entity.SeriesDict;
import com.bbplay.app.exception.BusinessException;
import com.bbplay.app.mapper.PictureBookMapper;
import com.bbplay.app.mapper.PictureBookPageMapper;
import com.bbplay.app.mapper.SeriesDictMapper;
import com.bbplay.app.service.PictureBookService;
import com.bbplay.app.vo.PictureBookAdminItemVO;
import com.bbplay.app.vo.PictureBookDetailVO;
import com.bbplay.app.vo.PictureBookFrontItemVO;
import com.bbplay.app.vo.PictureBookPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 绘本业务实现
 */
@Service
@RequiredArgsConstructor
public class PictureBookServiceImpl implements PictureBookService {

    private static final String SORT_BY_RATING = "rating";
    private static final String SORT_BY_CLICK = "click";
    private static final String ORDER_ASC = "asc";

    private final PictureBookMapper pictureBookMapper;
    private final PictureBookPageMapper pictureBookPageMapper;
    private final SeriesDictMapper seriesDictMapper;

    @Override
    public PageResult<PictureBookFrontItemVO> listFront(PictureBookListQuery query) {
        Page<PictureBook> page = new Page<>(query.getPage(), query.getPageSize());
        QueryWrapper<PictureBook> wrapper = new QueryWrapper<>();
        wrapper.eq("is_published", true).eq("is_abnormal", false);
        applyListFilter(wrapper, query.getAgeRange(), query.getSeriesId());
        applyKeyword(wrapper, query.getKeyword());
        applySort(wrapper, query.getSortBy(), query.getOrder());
        Page<PictureBook> result = pictureBookMapper.selectPage(page, wrapper);
        Map<Long, String> seriesNameMap = loadSeriesNames(collectSeriesIds(result.getRecords()));
        List<PictureBookFrontItemVO> records = result.getRecords().stream()
            .map(item -> PictureBookConverter.toFrontItem(item, seriesNameMap))
            .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public PictureBookDetailVO getFrontDetail(Long id) {
        PictureBook book = requireBook(id);
        if (!book.getIsPublished() || book.getIsAbnormal()) {
            throw new BusinessException("绘本不可用");
        }
        PictureBookDetailVO vo = PictureBookConverter.toDetail(book, loadSeriesNames(List.of(book.getSeriesId())));
        vo.setPages(loadPages(id));
        return vo;
    }

    @Override
    @Transactional
    public void addClick(Long id) {
        requireBook(id);
        LambdaUpdateWrapper<PictureBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PictureBook::getId, id)
            .setSql("click_count = COALESCE(click_count, 0) + 1")
            .set(PictureBook::getUpdatedAt, LocalDateTime.now());
        pictureBookMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void rate(Long id, Integer score) {
        PictureBook book = requireBook(id);
        if (!book.getIsPublished() || book.getIsAbnormal()) {
            throw new BusinessException("绘本不可评分");
        }
        updateRatingSummary(id, score);
    }

    @Override
    public PageResult<PictureBookAdminItemVO> listAdmin(AdminPictureBookListQuery query) {
        Page<PictureBook> page = new Page<>(query.getPage(), query.getPageSize());
        QueryWrapper<PictureBook> wrapper = new QueryWrapper<>();
        applyAdminFilter(wrapper, query.getSeriesId(), query.getIsPublished(), query.getIsAbnormal());
        applyKeyword(wrapper, query.getKeyword());
        wrapper.orderByDesc("updated_at");
        Page<PictureBook> result = pictureBookMapper.selectPage(page, wrapper);
        Map<Long, String> seriesNameMap = loadSeriesNames(collectSeriesIds(result.getRecords()));
        List<PictureBookAdminItemVO> records = result.getRecords().stream()
            .map(item -> PictureBookConverter.toAdminItem(item, seriesNameMap))
            .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public PictureBookDetailVO getAdminDetail(Long id) {
        PictureBook book = requireBook(id);
        PictureBookDetailVO vo = PictureBookConverter.toDetail(book, loadSeriesNames(List.of(book.getSeriesId())));
        vo.setPages(loadPages(id));
        return vo;
    }

    @Override
    @Transactional
    public Long create(PictureBookCreateRequest request) {
        ensureSeriesExists(request.getSeriesId());
        PictureBook book = new PictureBook();
        fillBook(request, book);
        LocalDateTime now = LocalDateTime.now();
        book.setPageCount(0);
        book.setClickCount(0L);
        book.setRatingAvg(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        book.setRatingCount(0);
        book.setCompleteReadCount(0L);
        book.setIsPublished(false);
        book.setIsAbnormal(false);
        book.setCreatedAt(now);
        book.setUpdatedAt(now);
        pictureBookMapper.insert(book);
        return book.getId();
    }

    @Override
    @Transactional
    public void update(Long id, PictureBookUpdateRequest request) {
        PictureBook existing = requireBook(id);
        ensureSeriesExists(request.getSeriesId());
        fillBook(request, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        pictureBookMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void updatePublish(Long id, Boolean published) {
        requireBook(id);
        LambdaUpdateWrapper<PictureBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PictureBook::getId, id)
            .set(PictureBook::getIsPublished, published)
            .set(PictureBook::getUpdatedAt, LocalDateTime.now());
        pictureBookMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void updateAbnormal(Long id, Boolean abnormal, String abnormalRemark) {
        requireBook(id);
        LambdaUpdateWrapper<PictureBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PictureBook::getId, id)
            .set(PictureBook::getIsAbnormal, abnormal)
            .set(PictureBook::getAbnormalRemark, abnormal ? abnormalRemark : null)
            .set(PictureBook::getUpdatedAt, LocalDateTime.now());
        pictureBookMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireBook(id);
        pictureBookMapper.deleteById(id);
        LambdaQueryWrapper<PictureBookPage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PictureBookPage::getBookId, id);
        pictureBookPageMapper.delete(wrapper);
    }

    private void updateRatingSummary(Long id, Integer score) {
        LambdaUpdateWrapper<PictureBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PictureBook::getId, id)
            .setSql("rating_avg = ROUND(((COALESCE(rating_avg, 0) * COALESCE(rating_count, 0)) + " + score
                + ") / (COALESCE(rating_count, 0) + 1), 2)")
            .setSql("rating_count = COALESCE(rating_count, 0) + 1")
            .set(PictureBook::getUpdatedAt, LocalDateTime.now());
        pictureBookMapper.update(null, wrapper);
    }

    private void applyListFilter(QueryWrapper<PictureBook> wrapper, String ageRange, Long seriesId) {
        if (StringUtils.hasText(ageRange)) {
            wrapper.eq("age_range", ageRange);
        }
        if (seriesId != null) {
            wrapper.eq("series_id", seriesId);
        }
    }

    private void applyAdminFilter(QueryWrapper<PictureBook> wrapper, Long seriesId, Boolean isPublished, Boolean isAbnormal) {
        if (seriesId != null) {
            wrapper.eq("series_id", seriesId);
        }
        if (isPublished != null) {
            wrapper.eq("is_published", isPublished);
        }
        if (isAbnormal != null) {
            wrapper.eq("is_abnormal", isAbnormal);
        }
    }

    private void applyKeyword(QueryWrapper<PictureBook> wrapper, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        List<Long> seriesIds = querySeriesIds(keyword);
        wrapper.and(w -> {
            w.like("title", keyword).or().like("nickname", keyword).or().like("alias", keyword);
            if (!seriesIds.isEmpty()) {
                w.or().in("series_id", seriesIds);
            }
        });
    }

    private void applySort(QueryWrapper<PictureBook> wrapper, String sortBy, String order) {
        boolean isAsc = ORDER_ASC.equalsIgnoreCase(order);
        if (SORT_BY_RATING.equals(sortBy)) {
            wrapper.orderBy(true, isAsc, "rating_avg").orderByDesc("created_at");
            return;
        }
        if (SORT_BY_CLICK.equals(sortBy)) {
            wrapper.orderBy(true, isAsc, "click_count").orderByDesc("created_at");
            return;
        }
        wrapper.orderByDesc("created_at");
    }

    private List<Long> querySeriesIds(String keyword) {
        LambdaQueryWrapper<SeriesDict> query = new LambdaQueryWrapper<>();
        query.like(SeriesDict::getName, keyword).or().like(SeriesDict::getAlias, keyword);
        return seriesDictMapper.selectList(query).stream().map(SeriesDict::getId).toList();
    }

    private Map<Long, String> loadSeriesNames(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<SeriesDict> query = new LambdaQueryWrapper<>();
        query.in(SeriesDict::getId, ids);
        return seriesDictMapper.selectList(query).stream()
            .collect(Collectors.toMap(SeriesDict::getId, SeriesDict::getName));
    }

    private List<Long> collectSeriesIds(List<PictureBook> books) {
        return books.stream().map(PictureBook::getSeriesId).distinct().toList();
    }

    private List<PictureBookPageVO> loadPages(Long bookId) {
        LambdaQueryWrapper<PictureBookPage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PictureBookPage::getBookId, bookId).orderByAsc(PictureBookPage::getPageNumber);
        return pictureBookPageMapper.selectList(wrapper).stream()
            .map(PictureBookConverter::toPageVO)
            .toList();
    }

    private PictureBook requireBook(Long id) {
        PictureBook book = pictureBookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException("绘本不存在");
        }
        return book;
    }

    private void ensureSeriesExists(Long seriesId) {
        SeriesDict series = seriesDictMapper.selectById(seriesId);
        if (series == null) {
            throw new BusinessException("系列不存在");
        }
    }

    private void fillBook(PictureBookCreateRequest request, PictureBook book) {
        book.setTitle(request.getTitle());
        book.setNickname(request.getNickname());
        book.setAlias(request.getAlias());
        book.setSeriesId(request.getSeriesId());
        book.setAgeRange(request.getAgeRange());
        book.setCoverUrl(request.getCoverUrl());
        book.setDescription(request.getDescription());
        book.setSortWeight(request.getSortWeight() != null ? request.getSortWeight() : 0);
    }

    private void fillBook(PictureBookUpdateRequest request, PictureBook book) {
        book.setTitle(request.getTitle());
        book.setNickname(request.getNickname());
        book.setAlias(request.getAlias());
        book.setSeriesId(request.getSeriesId());
        book.setAgeRange(request.getAgeRange());
        book.setCoverUrl(request.getCoverUrl());
        book.setDescription(request.getDescription());
        book.setSortWeight(request.getSortWeight() != null ? request.getSortWeight() : 0);
    }
}
