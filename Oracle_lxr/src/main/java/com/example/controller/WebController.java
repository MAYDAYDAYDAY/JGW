package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.User;
import com.example.common.config.JgwRuntimeConfig;
import com.example.service.UserService;
import com.example.utils.MailCodeCache;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 基础前端接口
 */
@RestController
public class WebController {

    @Resource
    private UserService userService;

    @Resource
    private JgwRuntimeConfig jgw;

    @GetMapping("/")
    public Result hello() {
        return Result.success("访问成功");
    }

    @PostMapping("/islogin")
    public Result islogin(){
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        if (ObjectUtil.isEmpty(user.getUsername()) || ObjectUtil.isEmpty(user.getPassword())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        user = userService.login(user);
//        System.out.println(user.getUsername()+" "+user.getPassword()+" "+user.getEmail());
//        System.out.println(user.getToken());
        return Result.success(user);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (ObjectUtil.isEmpty(user.getUsername()) || ObjectUtil.isEmpty(user.getPassword())
                || ObjectUtil.isEmpty(user.getEmail())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        if(!MailCodeCache.validateCode(user.getEmail(), user.getCode())){
            return Result.error(ResultCodeEnum.VALIDATE_CODE_ERROR);
        }
        System.out.println(user.getUsername()+" "+user.getPassword()+" "+user.getEmail());
        userService.register(user);
        return Result.success();
    }
    /**
     * 请求邮箱验证码
     */
    @GetMapping("/sendEmailCode")
    public Result sendEmailCode(@RequestParam String email) {
        if (StrUtil.isEmpty(email)) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        //生成六位随机数
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        String[] args1 = {
                jgw.getPythonExe(),
                jgw.getMailSenderScript().toString(),
                code,
                email
        };
        ProcessBuilder pb = new ProcessBuilder(args1);
        pb.directory(jgw.getMailSenderScript().getParent().toFile());
        pb.environment().put("PYTHONIOENCODING", "utf-8");

        try {
            Process proc = pb.start();
            int f = proc.waitFor();
            if (f == 0) {
                MailCodeCache.setCache(email, code);
            } else {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                    String actionStr = in.readLine();
                    if (actionStr != null) {
                        System.out.println(actionStr);
                    }
                }
                return Result.error(ResultCodeEnum.SEND_MAIL_ERROR);
            }

        } catch (Exception e) {
            // 打印异常堆栈信息。
            e.printStackTrace();
        }
        // 如果一切顺利，则返回成功结果。
        return Result.success();
    }


}
