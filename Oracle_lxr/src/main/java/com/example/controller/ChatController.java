package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Result;
import com.example.entity.ChatData;
import com.example.entity.Message;
import com.example.service.ChatService;
import com.example.utils.BigModelNew;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private ChatService chatService;

    //用户发送一条消息
    @PostMapping("/send")
    public Result send(@RequestBody ChatData chatData) {
        if(ObjectUtil.isNull(chatData)){
            return Result.error();
        }
        Message message = chatService.generateMessage(chatData);

        return Result.success(message);
    }

    //用户获取消息列表
    @GetMapping("/getdata/{sessionID}")
    public Result getData(@PathVariable Integer sessionID){
        if(ObjectUtil.isNull(sessionID)){
            return Result.error();
        }
        System.out.println("sessionID:  "+sessionID);
        List<Message> messages = chatService.getMessage(sessionID);
        System.out.println("Messages:"+messages.get(0));

        return Result.success(messages);
    }

    //用户获取SessionID 列表
    @GetMapping("/getsession/{userID}")
    public Result getSession(@PathVariable Integer userID){
        if(ObjectUtil.isNull(userID)){
            return Result.error();
        }
        List<Integer> sessionIDs = chatService.getSession(userID);
        return Result.success(sessionIDs);
    }

    // 创建任务
    @PostMapping("/createTask")
    public Result createTask(@RequestParam String imageBase64, @RequestParam String prompt) {
        System.out.println("12121212");
        try {
            System.out.println("afasfaa");

            String taskId = BigModelNew.createTask(imageBase64, prompt);
            System.out.println("controler:taskId:"+taskId);
            return Result.success(taskId);
        } catch (Exception e) {
            System.out.println("\n00000\n");
            e.printStackTrace();
            return Result.error();
        }
    }
    // 新增接口：查询任务
    @GetMapping("/queryTask/{taskId}")
    public Result queryTask(@PathVariable String taskId) {
        try {
            System.out.println("2222ada");
            java.util.Map<String, Object> result = BigModelNew.queryTask(taskId);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error();
        }
    }
}
