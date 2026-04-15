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
 * 绘本资源表
 */
@Data
@TableName("picture_book")
public class PictureBook {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 正式标题 */
    private String title;

    /** 自定义昵称 */
    private String nickname;

    /** 别名 */
    private String alias;

    /** 系列 ID */
    private Long seriesId;

    /** 适龄范围 */
    private String ageRange;

    /** 封面链接 */
    private String coverUrl;

    /** 简介 */
    private String description;

    /** 总页数 */
    private Integer pageCount;

    /** 是否上架 */
    private Boolean isPublished;

    /** 是否异常 */
    private Boolean isAbnormal;

    /** 异常原因 */
    private String abnormalRemark;

    /** 点击次数 */
    private Long clickCount;

    /** 平均评分 */
    private BigDecimal ratingAvg;

    /** 评分次数 */
    private Integer ratingCount;

    /** 完整阅读次数 */
    private Long completeReadCount;

    /** 排序权重 */
    private Integer sortWeight;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
