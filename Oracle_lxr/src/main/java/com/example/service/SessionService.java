package com.example.service;

import com.example.entity.Session;
import com.example.mapper.SessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SessionService {

    @Resource
    private SessionMapper sessionMapper;

    public void insertSession(Session session) {
        sessionMapper.insert(session);
    }

    public void deleteBySessionId(Integer sessionID) {
        sessionMapper.deleteBySessionId(sessionID);
    }

    public void deleteByUserId(Integer userID) {
        sessionMapper.deleteByUserId(userID);
    }

    public Session selectBySessionID(Integer sessionID) {
        return sessionMapper.selectById(sessionID);
    }


}
