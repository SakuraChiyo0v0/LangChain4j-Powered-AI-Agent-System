package com.hhu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_message")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Message {
    @TableId
    private Long id;
    private Long sessionId;
    private Integer tokens;
    @TableField(value = "user_msg")
    private String userMsg;
    @TableField(value = "ai_msg")
    private String AIMsg;
    private Long createdTime;
}
