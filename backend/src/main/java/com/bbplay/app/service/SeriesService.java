package com.bbplay.app.service;

import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.series.SeriesListQuery;
import com.bbplay.app.dto.series.SeriesSaveRequest;
import com.bbplay.app.vo.SeriesAdminVO;
import com.bbplay.app.vo.SeriesOptionVO;

import java.util.List;

/**
 * 系列字典业务服务。
 */
public interface SeriesService {

    List<SeriesOptionVO> listFrontOptions();

    PageResult<SeriesAdminVO> listAdmin(SeriesListQuery query);

    Long create(SeriesSaveRequest request);

    void update(Long id, SeriesSaveRequest request);
}
