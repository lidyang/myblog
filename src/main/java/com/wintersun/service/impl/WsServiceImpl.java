package com.wintersun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wintersun.entity.UserMessage;
import com.wintersun.service.UserMessageService;
import com.wintersun.service.WsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class WsServiceImpl implements WsService {


    @Autowired
    UserMessageService messageService;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Async
    @Override
    public void sendMessageCountToUser(Long toUserId) {
        System.out.println("sendMessageCountToUser+++++++++++++++=======");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int count = messageService.count(new QueryWrapper<UserMessage>()
                .eq("to_user_id", toUserId)
                .eq("status", "0")
        );

        // websocket通知 (/user/20/messCount)
        messagingTemplate.convertAndSendToUser(toUserId.toString(), "/messCount", count);
    }
}
