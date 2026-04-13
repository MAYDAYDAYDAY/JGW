package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Message;
import com.example.entity.Session;
import com.example.mapper.SessionMapper;
import com.example.mapper.UserMapper;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.utils.TokenUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.Integer.parseInt;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private SessionMapper sessionMapper;
    public User login(User user) {
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!dbUser.getPassword().equals(user.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        // 生成token
        String tokenData = dbUser.getId() + "-" + dbUser.getPassword();
        String token = TokenUtils.createToken(tokenData, dbUser.getPassword());
//        System.out.println("token:  "+token);
        dbUser.setToken(token);
        return dbUser;
    }

    public User selectById(Integer userId) {
        User dbUser = userMapper.selectById(userId);
        return dbUser;
    }

    public void updateById(User user){
        if(userMapper.selectById(user.getId())==null){
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        userMapper.updateById(user);
    }

    public void register(User user) {
        if (ObjectUtil.isNotNull(userMapper.selectByUsername(user.getUsername()))) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }

        userMapper.insert(user);

        User dbUser = userMapper.selectByUsername(user.getUsername());
        Session session = new Session();
        session.setUserID(dbUser.getId());
        session.setStartTime(FormatTime());
        System.out.println("session:  "+session.getStartTime() + " "+session.getUserID() + " "+session.getSessionID());
        sessionMapper.insert(session); //为新注册的用户创建一个会话

    }

    public void incScores(Integer id) {
        System.out.println("id:  "+id);
        User user = userMapper.selectById(id);
        user.setScores(user.getScores() + 1);
        userMapper.updateById(user);
    }


    public String FormatTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}
