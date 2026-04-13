package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
//        System.out.println("email:  "+user.getEmail());
        userService.updateById(user);

        return Result.success();
    }

    @GetMapping("/getUserInfo")
    public Result getUserInfo(@RequestParam Integer userId) {
//        System.out.println("userId:  "+userId);
        User user = userService.selectById(userId);
        return Result.success(user);
    }


}
