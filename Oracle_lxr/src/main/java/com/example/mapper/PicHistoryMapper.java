package com.example.mapper;

import com.example.entity.PicHistory;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PicHistoryMapper {

    List<PicHistory>selectById(Integer id);

    int insert(PicHistory picHistory);
}
