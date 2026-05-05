package com.bbplay.app.service;

import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.PictureBookCreateRequest;
import com.bbplay.app.dto.PictureBookUpdateRequest;
import com.bbplay.app.dto.picturebook.AdminPictureBookListQuery;
import com.bbplay.app.dto.picturebook.PictureBookListQuery;
import com.bbplay.app.vo.PictureBookAdminItemVO;
import com.bbplay.app.vo.PictureBookDetailVO;
import com.bbplay.app.vo.PictureBookFrontItemVO;
import com.bbplay.app.vo.PictureBookPageVO;

import java.util.List;

/**
 * 绘本业务服务
 */
public interface PictureBookService {

    PageResult<PictureBookFrontItemVO> listFront(PictureBookListQuery query);

    PictureBookDetailVO getFrontDetail(Long id);

    /**
     * 查询前台绘本全部页面，供阅读页按双页模式加载。
     */
    List<PictureBookPageVO> listFrontPages(Long id);

    void addClick(Long id);

    void rate(Long id, Integer score);

    PageResult<PictureBookAdminItemVO> listAdmin(AdminPictureBookListQuery query);

    PictureBookDetailVO getAdminDetail(Long id);

    Long create(PictureBookCreateRequest request);

    void update(Long id, PictureBookUpdateRequest request);

    void updatePublish(Long id, Boolean published);

    void updateAbnormal(Long id, Boolean abnormal, String abnormalRemark);

    void delete(Long id);
}
