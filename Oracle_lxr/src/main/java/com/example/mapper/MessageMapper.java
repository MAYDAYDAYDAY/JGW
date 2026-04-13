package com.example.mapper;

import com.example.entity.Message;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

public interface MessageMapper {
    int updateById(Message message);
    int insert(Message message);

//    @Select("select * from message where sessionID = #{sessionID}")
    @Select("select * from message where sessionID = #{sessionID} ")
    List<Message> selectBySessionId(Integer sessionID);


}
