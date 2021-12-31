package com.nowcode.community.dao;

import com.nowcode.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //orderMode=0,按照创建时间排，1按照热度排
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode);

    //方法中只有一个参数，并且在<if>中使用，必须加别名
    // param用来提供别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

    int updateType(int id,int type);

    int updateStatus(int id,int status);

    int updateScore(int id,double score);

}
