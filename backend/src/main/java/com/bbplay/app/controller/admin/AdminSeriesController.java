package com.bbplay.app.controller.admin;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.series.SeriesListQuery;
import com.bbplay.app.dto.series.SeriesSaveRequest;
import com.bbplay.app.service.SeriesService;
import com.bbplay.app.vo.SeriesAdminVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端系列字典接口。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/admin/series")
public class AdminSeriesController {

    private final SeriesService seriesService;

    /**
     * 系列分页查询。
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<SeriesAdminVO>> list(@Valid @ModelAttribute SeriesListQuery query) {
        return ApiResponse.success(seriesService.listAdmin(query));
    }

    /**
     * 新增系列。
     */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody SeriesSaveRequest request) {
        Long id = seriesService.create(request);
        return ApiResponse.success(Map.of("id", id));
    }

    /**
     * 编辑系列。
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody SeriesSaveRequest request) {
        seriesService.update(id, request);
        return ApiResponse.success();
    }
}
