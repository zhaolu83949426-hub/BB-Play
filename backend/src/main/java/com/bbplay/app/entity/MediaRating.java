package com.bbplay.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源评分表，记录匿名评分明细。
 */
@Data
@TableName("media_rating")
public class MediaRating {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long mediaId;
    private Integer score;
    private String deviceId;
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
