package com.bbplay.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.series.SeriesListQuery;
import com.bbplay.app.dto.series.SeriesSaveRequest;
import com.bbplay.app.entity.SeriesDict;
import com.bbplay.app.exception.BusinessException;
import com.bbplay.app.mapper.MediaResourceMapper;
import com.bbplay.app.mapper.SeriesDictMapper;
import com.bbplay.app.service.SeriesService;
import com.bbplay.app.vo.SeriesAdminVO;
import com.bbplay.app.vo.SeriesCountRow;
import com.bbplay.app.vo.SeriesOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系列字典业务实现。
 */
@Service
@RequiredArgsConstructor
public class SeriesServiceImpl implements SeriesService {

    private final SeriesDictMapper seriesDictMapper;
    private final MediaResourceMapper mediaResourceMapper;

    @Override
    public List<SeriesOptionVO> listFrontOptions() {
        LambdaQueryWrapper<SeriesDict> query = new LambdaQueryWrapper<>();
        query.eq(SeriesDict::getIsEnabled, true)
            .orderByAsc(SeriesDict::getSortWeight)
            .orderByAsc(SeriesDict::getCreatedAt);
        return seriesDictMapper.selectList(query).stream()
            .map(it -> new SeriesOptionVO(it.getId(), it.getName()))
            .toList();
    }

    @Override
    public PageResult<SeriesAdminVO> listAdmin(SeriesListQuery query) {
        Page<SeriesDict> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<SeriesDict> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SeriesDict::getName, query.getKeyword())
                .or()
                .like(SeriesDict::getAlias, query.getKeyword()));
        }
        if (query.getEnabled() != null) {
            wrapper.eq(SeriesDict::getIsEnabled, query.getEnabled());
        }
        wrapper.orderByAsc(SeriesDict::getSortWeight).orderByDesc(SeriesDict::getCreatedAt);
        Page<SeriesDict> result = seriesDictMapper.selectPage(page, wrapper);
        Map<Long, Long> countMap = mediaResourceMapper.countBySeries().stream()
            .collect(Collectors.toMap(SeriesCountRow::getSeriesId, SeriesCountRow::getTotal));
        List<SeriesAdminVO> records = result.getRecords().stream()
            .map(item -> toSeriesAdmin(item, countMap))
            .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    @Transactional
    public Long create(SeriesSaveRequest request) {
        ensureNameUnique(request.getName(), null);
        SeriesDict series = new SeriesDict();
        fillSeries(request, series);
        LocalDateTime now = LocalDateTime.now();
        series.setCreatedAt(now);
        series.setUpdatedAt(now);
        seriesDictMapper.insert(series);
        return series.getId();
    }

    @Override
    @Transactional
    public void update(Long id, SeriesSaveRequest request) {
        SeriesDict existing = seriesDictMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("系列不存在");
        }
        ensureNameUnique(request.getName(), id);
        fillSeries(request, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        seriesDictMapper.updateById(existing);
    }

    private void ensureNameUnique(String name, Long ignoreId) {
        LambdaQueryWrapper<SeriesDict> query = new LambdaQueryWrapper<>();
        query.eq(SeriesDict::getName, name);
        List<SeriesDict> exists = seriesDictMapper.selectList(query);
        if (exists.isEmpty()) {
            return;
        }
        if (ignoreId != null && exists.stream().allMatch(it -> it.getId().equals(ignoreId))) {
            return;
        }
        throw new BusinessException("系列名称已存在");
    }

    private SeriesAdminVO toSeriesAdmin(SeriesDict item, Map<Long, Long> countMap) {
        SeriesAdminVO vo = new SeriesAdminVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setAlias(item.getAlias());
        vo.setIsEnabled(item.getIsEnabled());
        vo.setSortWeight(item.getSortWeight());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setResourceCount(countMap.getOrDefault(item.getId(), 0L));
        return vo;
    }

    private void fillSeries(SeriesSaveRequest request, SeriesDict series) {
        series.setName(request.getName());
        series.setAlias(request.getAlias());
        series.setIsEnabled(request.getEnabled());
        series.setSortWeight(request.getSortWeight());
    }
}
