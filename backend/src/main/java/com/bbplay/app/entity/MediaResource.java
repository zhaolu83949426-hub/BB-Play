package com.bbplay.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资源表，维护前台播放与管理所需主数据。
 */
@Data
@TableName("media_resource")
public class MediaResource {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String title;
    private String nickname;
    private String alias;
    private Long seriesId;
    private String ageRange;
    private String mediaType;
    private String playUrl;
    private String coverUrl;
    private String description;
    private String sourceType;
    private String sourcePlatform;
    private String sourceRemark;
    private Boolean isPublished;
    private Boolean isAbnormal;
    private String abnormalRemark;
    private Long clickCount;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private Integer sortWeight;
    private String lastTestStatus;
    private Integer lastTestCode;
    private Integer lastTestLatencyMs;
    private String lastTestError;
    private LocalDateTime lastTestAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
