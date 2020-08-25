package com.wintersun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    Date created;

    Date modified;

    Integer status;


}
