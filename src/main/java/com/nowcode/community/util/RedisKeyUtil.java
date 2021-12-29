package com.nowcode.community.util;

//redis的key通过：进行连接单词
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    //帖子或评论
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";
    //主动关注别人的用户，粉丝
    private static final String PREFIX_FOLLOWEE = "followee";
    //关注的目标，偶像
    private static final String PREFIX_FOLLOWER = "follower";

    //验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    //凭证
    private static final String PREFIX_TICKET = "ticket";
    //缓存用户
    private static final String PREFIX_USER = "user";
    //UV，独立用户
    private static final String PREFIX_UV = "uv";
    //DAU，日访问用户
    private static final String PREFIX_DAU = "dau";

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

    //用户关注的实体，可能是偶像、评论、帖子
    //followee:userId:entityType -> zset(entityId,now)
    public static String getPrefixFollowee(int followeeId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + followeeId + SPLIT + entityType;
    }

    //某个实体（用户、帖子）拥有的粉丝
    //follower:entityType:entityId->zst(userId,now)
    public static String getPrefixFollower(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //登录验证码
    //临时凭证
    public static String getPrefixKaptcha(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录凭证ticket
    public static String getPrefixTicket(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //缓存用户
    public static String getPrefixUser(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    //返回单日uv
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    //区间uv
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //单日dau活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    //区间dau活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

}
