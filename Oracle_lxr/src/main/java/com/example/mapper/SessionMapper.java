package com.example.mapper;

import com.example.entity.Session;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SessionMapper {


    int insert(Session session);

    @Delete("delete from session where sessionID = #{sessionID}")
    int deleteBySessionId(Integer sessionID);

    @Delete("delete from session where userID = #{userID}")
    int deleteByUserId(Integer userID);


    int updateById(Session session);


    @Select("select * from session where sessionID = #{sessionID}")
    Session selectById(Integer sessionID);

    List<Session> selectByUserId(Integer userID);

    @Select("select sessionID from session where userID = #{userID}")
    List<Integer>selelctSessionIdByUserId(Integer userID);
}
