package com.wintersun.im.message;

import com.wintersun.im.vo.ImTo;
import com.wintersun.im.vo.ImUser;
import lombok.Data;

@Data
public class ChatImMess {

    private ImUser mine;
    private ImTo to;

}
