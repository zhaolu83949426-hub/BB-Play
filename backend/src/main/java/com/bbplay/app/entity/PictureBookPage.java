package com.bbplay.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 绘本页面表
 */
@Data
@TableName("picture_book_page")
public class PictureBookPage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联绘本 ID */
    private Long bookId;

    /** 页码 */
    private Integer pageNumber;

    /** 图片地址 */
    private String imageUrl;

    /** 页面文字 */
    private String textContent;

    /** 预生成的音频地址（可选） */
    private String audioUrl;

    /** 朗读时长（秒） */
    private Integer durationSec;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
