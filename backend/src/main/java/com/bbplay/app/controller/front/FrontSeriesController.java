package com.bbplay.app.controller.front;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.service.SeriesService;
import com.bbplay.app.vo.SeriesOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台系列筛选接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/series")
public class FrontSeriesController {

    private final SeriesService seriesService;

    /**
     * 返回启用系列列表。
     */
    @GetMapping("/options")
    public ApiResponse<List<SeriesOptionVO>> options() {
        return ApiResponse.success(seriesService.listFrontOptions());
    }
}
