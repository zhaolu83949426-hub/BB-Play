package com.bbplay.app.service.impl;

import com.bbplay.app.entity.MediaResource;
import com.bbplay.app.vo.MediaAdminItemVO;
import com.bbplay.app.vo.MediaDetailVO;
import com.bbplay.app.vo.MediaFrontItemVO;

import java.util.Map;

/**
 * 资源领域对象转换器。
 */
public final class MediaConverter {

    private MediaConverter() {
    }

    public static MediaFrontItemVO toFrontItem(MediaResource item, Map<Long, String> seriesNameMap) {
        MediaFrontItemVO vo = new MediaFrontItemVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setNickname(item.getNickname());
        vo.setAlias(item.getAlias());
        vo.setSeries(seriesNameMap.get(item.getSeriesId()));
        vo.setAgeRange(item.getAgeRange());
        vo.setMediaType(item.getMediaType());
        vo.setPlayUrl(item.getPlayUrl());
        vo.setCoverUrl(item.getCoverUrl());
        vo.setClickCount(item.getClickCount());
        vo.setRatingAvg(item.getRatingAvg());
        vo.setRatingCount(item.getRatingCount());
        return vo;
    }

    public static MediaAdminItemVO toAdminItem(MediaResource item, Map<Long, String> seriesNameMap) {
        MediaAdminItemVO vo = new MediaAdminItemVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setNickname(item.getNickname());
        vo.setMediaType(item.getMediaType());
        vo.setSeries(seriesNameMap.get(item.getSeriesId()));
        vo.setAgeRange(item.getAgeRange());
        vo.setIsPublished(item.getIsPublished());
        vo.setIsAbnormal(item.getIsAbnormal());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setClickCount(item.getClickCount());
        vo.setRatingAvg(item.getRatingAvg());
        vo.setRatingCount(item.getRatingCount());
        return vo;
    }

    public static MediaDetailVO toDetail(MediaResource item, Map<Long, String> seriesNameMap) {
        MediaDetailVO vo = new MediaDetailVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setNickname(item.getNickname());
        vo.setAlias(item.getAlias());
        vo.setSeriesId(item.getSeriesId());
        vo.setSeries(seriesNameMap.get(item.getSeriesId()));
        vo.setAgeRange(item.getAgeRange());
        vo.setMediaType(item.getMediaType());
        vo.setPlayUrl(item.getPlayUrl());
        vo.setCoverUrl(item.getCoverUrl());
        vo.setDescription(item.getDescription());
        vo.setIsPublished(item.getIsPublished());
        vo.setIsAbnormal(item.getIsAbnormal());
        vo.setAbnormalRemark(item.getAbnormalRemark());
        vo.setSourceRemark(item.getSourceRemark());
        vo.setSortWeight(item.getSortWeight());
        vo.setClickCount(item.getClickCount());
        vo.setRatingAvg(item.getRatingAvg());
        vo.setRatingCount(item.getRatingCount());
        return vo;
    }
}
