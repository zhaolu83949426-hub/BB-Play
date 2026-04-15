package com.bbplay.app.service.impl;

import com.bbplay.app.entity.PictureBook;
import com.bbplay.app.entity.PictureBookPage;
import com.bbplay.app.vo.PictureBookAdminItemVO;
import com.bbplay.app.vo.PictureBookDetailVO;
import com.bbplay.app.vo.PictureBookFrontItemVO;
import com.bbplay.app.vo.PictureBookPageVO;

import java.util.Map;

/**
 * 绘本领域对象转换器
 */
public final class PictureBookConverter {

    private PictureBookConverter() {
    }

    public static PictureBookFrontItemVO toFrontItem(PictureBook item, Map<Long, String> seriesNameMap) {
        PictureBookFrontItemVO vo = new PictureBookFrontItemVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setNickname(item.getNickname());
        vo.setAlias(item.getAlias());
        vo.setSeries(seriesNameMap.get(item.getSeriesId()));
        vo.setAgeRange(item.getAgeRange());
        vo.setCoverUrl(item.getCoverUrl());
        vo.setPageCount(item.getPageCount());
        vo.setClickCount(item.getClickCount());
        vo.setRatingAvg(item.getRatingAvg());
        vo.setRatingCount(item.getRatingCount());
        return vo;
    }

    public static PictureBookAdminItemVO toAdminItem(PictureBook item, Map<Long, String> seriesNameMap) {
        PictureBookAdminItemVO vo = new PictureBookAdminItemVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setNickname(item.getNickname());
        vo.setSeries(seriesNameMap.get(item.getSeriesId()));
        vo.setAgeRange(item.getAgeRange());
        vo.setPageCount(item.getPageCount());
        vo.setIsPublished(item.getIsPublished());
        vo.setIsAbnormal(item.getIsAbnormal());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setClickCount(item.getClickCount());
        vo.setRatingAvg(item.getRatingAvg());
        vo.setRatingCount(item.getRatingCount());
        return vo;
    }

    public static PictureBookDetailVO toDetail(PictureBook item, Map<Long, String> seriesNameMap) {
        PictureBookDetailVO vo = new PictureBookDetailVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setNickname(item.getNickname());
        vo.setAlias(item.getAlias());
        vo.setSeriesId(item.getSeriesId());
        vo.setSeries(seriesNameMap.get(item.getSeriesId()));
        vo.setAgeRange(item.getAgeRange());
        vo.setCoverUrl(item.getCoverUrl());
        vo.setDescription(item.getDescription());
        vo.setSortWeight(item.getSortWeight());
        vo.setPageCount(item.getPageCount());
        vo.setClickCount(item.getClickCount());
        vo.setRatingAvg(item.getRatingAvg());
        vo.setRatingCount(item.getRatingCount());
        return vo;
    }

    public static PictureBookPageVO toPageVO(PictureBookPage page) {
        PictureBookPageVO vo = new PictureBookPageVO();
        vo.setId(page.getId());
        vo.setBookId(page.getBookId());
        vo.setPageNumber(page.getPageNumber());
        vo.setImageUrl(page.getImageUrl());
        vo.setTextContent(page.getTextContent());
        vo.setAudioUrl(page.getAudioUrl());
        vo.setDurationSec(page.getDurationSec());
        return vo;
    }
}
