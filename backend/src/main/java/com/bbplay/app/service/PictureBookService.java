package com.bbplay.app.service;

import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.PictureBookCreateRequest;
import com.bbplay.app.dto.PictureBookUpdateRequest;
import com.bbplay.app.dto.picturebook.AdminPictureBookListQuery;
import com.bbplay.app.dto.picturebook.PictureBookListQuery;
import com.bbplay.app.vo.PictureBookAdminItemVO;
import com.bbplay.app.vo.PictureBookDetailVO;
import com.bbplay.app.vo.PictureBookFrontItemVO;

/**
 * 绘本业务服务
 */
public interface PictureBookService {

    PageResult<PictureBookFrontItemVO> listFront(PictureBookListQuery query);

    PictureBookDetailVO getFrontDetail(Long id);

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
