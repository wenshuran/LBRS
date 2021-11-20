package com.gatech.bigdata.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_vector")
public class UserVector {
    private int id;
    private String tag;
    private Short used;
}
