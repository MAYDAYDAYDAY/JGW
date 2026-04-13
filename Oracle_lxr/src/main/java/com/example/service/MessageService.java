package com.example.service;

import com.example.entity.Message;
import com.example.entity.Session;
import com.example.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessageService {
    @Resource
    private MessageMapper messageMapper;
    public void insertMessage(Message message){
        messageMapper.insert(message);
    }

    public void selectBySessionId(Integer sessionID){
        messageMapper.selectBySessionId(sessionID);
    }

}
