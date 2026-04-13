package com.example.service;

import com.example.entity.PicHistory;
import com.example.mapper.PicHistoryMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PicHistoryService {

    @Resource
    private PicHistoryMapper picHistoryMapper;

    public List<PicHistory> getPicHistoryById(Integer id){
        return picHistoryMapper.selectById(id);
    }

    public int addPicHistory(PicHistory picHistory){
        return picHistoryMapper.insert(picHistory);
    }
}
