package com.bbplay.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bbplay.app.entity.MediaResource;
import com.bbplay.app.vo.SeriesCountRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MediaResourceMapper extends BaseMapper<MediaResource> {

    @Select("""
        SELECT series_id AS seriesId, COUNT(1) AS total
        FROM media_resource
        WHERE deleted = 0
        GROUP BY series_id
        """)
    List<SeriesCountRow> countBySeries();
}
