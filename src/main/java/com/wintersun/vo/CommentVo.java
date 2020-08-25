package com.wintersun.vo;

import com.wintersun.entity.Comment;
import com.wintersun.entity.Post;
import lombok.Data;

@Data
public class CommentVo extends Comment {

    private Long authorId;
    private String authorName;
    private String authorAvatar;


}
