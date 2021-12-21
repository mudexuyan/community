package com.nowcode.community.util;

//redis的key通过：进行连接单词
public class RedisKeyUtil {

    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    //某个实体的赞
    //like:entity:entityType:entityId   -》 set(userId)，帖子和评论保存点赞用户的id
    public static String getPrefixEntityLike(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    //like:user:userId -> int
    public static String getPrefixUserLike(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
}
