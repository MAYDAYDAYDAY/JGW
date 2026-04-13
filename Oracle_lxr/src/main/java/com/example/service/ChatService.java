package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.ChatData;
import com.example.entity.Message;
import com.example.exception.CustomException;
import com.example.mapper.MessageMapper;
import com.example.mapper.SessionMapper;
import com.example.utils.BigModelNew;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class ChatService {

    @Resource
    private SessionMapper sessionMapper;

    @Resource
    private MessageMapper messageMapper;

    public Message generateMessage(ChatData chatData){

        if(ObjectUtil.isNull(chatData.getMessageList()) || ObjectUtil.isNull(chatData.getSession())){
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        //获取最后一条消息
        List<Message> messages = chatData.getMessageList();
        Message lastMessage = messages.get(messages.size()-1);
        lastMessage.setWho(1);//1代表user
        String time1 = FormatTime();
        lastMessage.setTimePoint(time1);
        //获取会话ID
        Integer sessionID = chatData.getSession().getSessionID();
        messageMapper.insert(lastMessage); //将用户消息插入数据库


//        ExecutorService executor = Executors.newFixedThreadPool(1); // 或者根据需要调整线程池的大小
//        Future<Message> future = executor.submit(() -> {
//            BigModelNew.Start(chatData);
//            return BigModelNew.modelAnswer;
//        });

        //发送给大模型
        Message message = new Message();
        try{
//           message = future.get();
            message = BigModelNew.Start(chatData,lastMessage);
            message.setSessionID(sessionID);
            System.out.println("message:"+message.getContent());
        }catch (Exception e){
            e.printStackTrace();
            throw new CustomException(ResultCodeEnum.BIGMODEL_ERROR);
        }


        messageMapper.insert(message);//将大模型消息插入数据库


        return message;
    }

    public List<Message> getMessage(Integer sessionID){
        return messageMapper.selectBySessionId(sessionID);
    }

    public List<Integer> getSession(Integer userID){
        return sessionMapper.selelctSessionIdByUserId(userID);
    }

    public String FormatTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}
