package com.bbplay.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系列字典表，统一资源系列名称与启用状态。
 */
@Data
@TableName("series_dict")
public class SeriesDict {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String alias;
    private Boolean isEnabled;
    private Integer sortWeight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
