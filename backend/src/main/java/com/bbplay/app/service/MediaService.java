package com.bbplay.app.service;

import com.bbplay.app.common.PageResult;
import com.bbplay.app.dto.media.AdminMediaListQuery;
import com.bbplay.app.dto.media.MediaListQuery;
import com.bbplay.app.dto.media.MediaSaveRequest;
import com.bbplay.app.vo.MediaAdminItemVO;
import com.bbplay.app.vo.MediaDetailVO;
import com.bbplay.app.vo.MediaFrontItemVO;

/**
 * 资源业务服务。
 */
public interface MediaService {

    PageResult<MediaFrontItemVO> listFront(MediaListQuery query);

    MediaDetailVO getFrontDetail(Long id);

    void addClick(Long id);

    void rate(Long id, Integer score);

    PageResult<MediaAdminItemVO> listAdmin(AdminMediaListQuery query);

    MediaDetailVO getAdminDetail(Long id);

    Long create(MediaSaveRequest request);

    void update(Long id, MediaSaveRequest request);

    void updatePublish(Long id, Boolean published);

    void updateAbnormal(Long id, Boolean abnormal, String abnormalRemark);
}
