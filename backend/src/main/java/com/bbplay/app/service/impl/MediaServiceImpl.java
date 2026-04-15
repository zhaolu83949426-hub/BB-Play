package com.bbplay.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.media.AdminMediaListQuery;
import com.bbplay.app.dto.media.MediaListQuery;
import com.bbplay.app.dto.media.MediaSaveRequest;
import com.bbplay.app.entity.MediaRating;
import com.bbplay.app.entity.MediaResource;
import com.bbplay.app.entity.SeriesDict;
import com.bbplay.app.exception.BusinessException;
import com.bbplay.app.mapper.MediaRatingMapper;
import com.bbplay.app.mapper.MediaResourceMapper;
import com.bbplay.app.mapper.SeriesDictMapper;
import com.bbplay.app.service.MediaService;
import com.bbplay.app.vo.MediaAdminItemVO;
import com.bbplay.app.vo.MediaDetailVO;
import com.bbplay.app.vo.MediaFrontItemVO;
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
 * 资源业务实现，负责前台展示与管理操作。
 */
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final String SORT_BY_RATING = "rating";
    private static final String SORT_BY_CLICK = "click";
    private static final String ORDER_ASC = "asc";

    private final MediaResourceMapper mediaResourceMapper;
    private final MediaRatingMapper mediaRatingMapper;
    private final SeriesDictMapper seriesDictMapper;

    @Override
    public PageResult<MediaFrontItemVO> listFront(MediaListQuery query) {
        Page<MediaResource> page = new Page<>(query.getPage(), query.getPageSize());
        QueryWrapper<MediaResource> wrapper = new QueryWrapper<>();
        wrapper.eq("is_published", true).eq("is_abnormal", false);
        applyListFilter(wrapper, query.getMediaType(), query.getAgeRange(), query.getSeriesId());
        applyKeyword(wrapper, query.getKeyword());
        applySort(wrapper, query.getSortBy(), query.getOrder());
        Page<MediaResource> result = mediaResourceMapper.selectPage(page, wrapper);
        Map<Long, String> seriesNameMap = loadSeriesNames(collectSeriesIds(result.getRecords()));
        List<MediaFrontItemVO> records = result.getRecords().stream()
            .map(item -> MediaConverter.toFrontItem(item, seriesNameMap))
            .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public MediaDetailVO getFrontDetail(Long id) {
        MediaResource resource = requireMedia(id);
        if (!resource.getIsPublished() || resource.getIsAbnormal()) {
            throw new BusinessException("资源不可用");
        }
        return MediaConverter.toDetail(resource, loadSeriesNames(List.of(resource.getSeriesId())));
    }

    @Override
    @Transactional
    public void addClick(Long id) {
        requireMedia(id);
        LambdaUpdateWrapper<MediaResource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MediaResource::getId, id)
            .setSql("click_count = COALESCE(click_count, 0) + 1")
            .set(MediaResource::getUpdatedAt, LocalDateTime.now());
        mediaResourceMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void rate(Long id, Integer score) {
        MediaResource resource = requireMedia(id);
        if (!resource.getIsPublished() || resource.getIsAbnormal()) {
            throw new BusinessException("资源不可评分");
        }
        // 先落评分明细，保证后续可追溯每次匿名评分记录。
        MediaRating rating = new MediaRating();
        rating.setMediaId(id);
        rating.setScore(score);
        rating.setCreatedAt(LocalDateTime.now());
        mediaRatingMapper.insert(rating);
        // 再原子更新均分与评分次数，保持聚合字段与明细一致。
        updateRatingSummary(id, score);
    }

    @Override
    public PageResult<MediaAdminItemVO> listAdmin(AdminMediaListQuery query) {
        Page<MediaResource> page = new Page<>(query.getPage(), query.getPageSize());
        QueryWrapper<MediaResource> wrapper = new QueryWrapper<>();
        applyAdminFilter(wrapper, query.getMediaType(), query.getSeriesId(), query.getStatus());
        applyKeyword(wrapper, query.getKeyword());
        wrapper.orderByDesc("updated_at");
        Page<MediaResource> result = mediaResourceMapper.selectPage(page, wrapper);
        Map<Long, String> seriesNameMap = loadSeriesNames(collectSeriesIds(result.getRecords()));
        List<MediaAdminItemVO> records = result.getRecords().stream()
            .map(item -> MediaConverter.toAdminItem(item, seriesNameMap))
            .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public MediaDetailVO getAdminDetail(Long id) {
        MediaResource resource = requireMedia(id);
        return MediaConverter.toDetail(resource, loadSeriesNames(List.of(resource.getSeriesId())));
    }

    @Override
    @Transactional
    public Long create(MediaSaveRequest request) {
        ensureSeriesExists(request.getSeriesId());
        MediaResource resource = new MediaResource();
        fillResource(request, resource);
        LocalDateTime now = LocalDateTime.now();
        resource.setClickCount(0L);
        resource.setRatingAvg(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        resource.setRatingCount(0);
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);
        mediaResourceMapper.insert(resource);
        return resource.getId();
    }

    @Override
    @Transactional
    public void update(Long id, MediaSaveRequest request) {
        MediaResource existing = requireMedia(id);
        ensureSeriesExists(request.getSeriesId());
        fillResource(request, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        mediaResourceMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void updatePublish(Long id, Boolean published) {
        requireMedia(id);
        LambdaUpdateWrapper<MediaResource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MediaResource::getId, id)
            .set(MediaResource::getIsPublished, published)
            .set(MediaResource::getUpdatedAt, LocalDateTime.now());
        mediaResourceMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void updateAbnormal(Long id, Boolean abnormal, String abnormalRemark) {
        requireMedia(id);
        LambdaUpdateWrapper<MediaResource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MediaResource::getId, id)
            .set(MediaResource::getIsAbnormal, abnormal)
            .set(MediaResource::getAbnormalRemark, abnormal ? abnormalRemark : null)
            .set(MediaResource::getUpdatedAt, LocalDateTime.now());
        mediaResourceMapper.update(null, wrapper);
    }

    private void updateRatingSummary(Long id, Integer score) {
        LambdaUpdateWrapper<MediaResource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MediaResource::getId, id)
            .setSql("rating_avg = ROUND(((COALESCE(rating_avg, 0) * COALESCE(rating_count, 0)) + " + score
                + ") / (COALESCE(rating_count, 0) + 1), 2)")
            .setSql("rating_count = COALESCE(rating_count, 0) + 1")
            .set(MediaResource::getUpdatedAt, LocalDateTime.now());
        mediaResourceMapper.update(null, wrapper);
    }

    private void applyListFilter(QueryWrapper<MediaResource> wrapper, String mediaType, String ageRange, Long seriesId) {
        if (StringUtils.hasText(mediaType)) {
            wrapper.eq("media_type", mediaType);
        }
        if (StringUtils.hasText(ageRange)) {
            wrapper.eq("age_range", ageRange);
        }
        if (seriesId != null) {
            wrapper.eq("series_id", seriesId);
        }
    }

    private void applyAdminFilter(QueryWrapper<MediaResource> wrapper, String mediaType, Long seriesId, String status) {
        if (StringUtils.hasText(mediaType)) {
            wrapper.eq("media_type", mediaType);
        }
        if (seriesId != null) {
            wrapper.eq("series_id", seriesId);
        }
        if (!StringUtils.hasText(status)) {
            return;
        }
        if ("published".equals(status)) {
            wrapper.eq("is_published", true);
        } else if ("unpublished".equals(status)) {
            wrapper.eq("is_published", false);
        } else if ("abnormal".equals(status)) {
            wrapper.eq("is_abnormal", true);
        } else if ("normal".equals(status)) {
            wrapper.eq("is_abnormal", false);
        }
    }

    private void applyKeyword(QueryWrapper<MediaResource> wrapper, String keyword) {
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

    private void applySort(QueryWrapper<MediaResource> wrapper, String sortBy, String order) {
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
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<SeriesDict> query = new LambdaQueryWrapper<>();
        query.in(SeriesDict::getId, ids);
        return seriesDictMapper.selectList(query).stream()
            .collect(Collectors.toMap(SeriesDict::getId, SeriesDict::getName, (v1, v2) -> v1));
    }

    private Collection<Long> collectSeriesIds(List<MediaResource> records) {
        return records.stream().map(MediaResource::getSeriesId).collect(Collectors.toSet());
    }

    private MediaResource requireMedia(Long id) {
        MediaResource resource = mediaResourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        return resource;
    }

    private void ensureSeriesExists(Long seriesId) {
        SeriesDict series = seriesDictMapper.selectById(seriesId);
        if (series == null) {
            throw new BusinessException("系列不存在");
        }
    }

    private void fillResource(MediaSaveRequest request, MediaResource resource) {
        resource.setTitle(request.getTitle());
        resource.setNickname(request.getNickname());
        resource.setAlias(request.getAlias());
        resource.setSeriesId(request.getSeriesId());
        resource.setAgeRange(request.getAgeRange());
        resource.setMediaType(request.getMediaType());
        resource.setPlayUrl(request.getPlayUrl());
        resource.setCoverUrl(request.getCoverUrl());
        resource.setDescription(request.getDescription());
        resource.setIsPublished(request.getIsPublished());
        resource.setIsAbnormal(request.getIsAbnormal());
        resource.setAbnormalRemark(request.getAbnormalRemark());
        resource.setSourceRemark(request.getSourceRemark());
        resource.setSortWeight(request.getSortWeight());
    }
}
