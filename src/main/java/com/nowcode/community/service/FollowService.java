package com.nowcode.community.service;

import com.nowcode.community.entity.User;
import com.nowcode.community.util.CommunityConstant;
import com.nowcode.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //两次存储，事务管理
    public void follow(int userId, int entityType, int entityId) {

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //粉丝
                String followee = RedisKeyUtil.getPrefixFollowee(userId, entityType);
                //偶像
                String follower = RedisKeyUtil.getPrefixFollower(entityType, entityId);

                operations.multi();
                operations.opsForZSet().add(followee, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(follower, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    //两次存储，事务管理
    public void unFollow(int userId, int entityType, int entityId) {

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //粉丝
                String followee = RedisKeyUtil.getPrefixFollowee(userId, entityType);
                //偶像
                String follower = RedisKeyUtil.getPrefixFollower(entityType, entityId);

                operations.multi();
                operations.opsForZSet().remove(followee, entityId);
                operations.opsForZSet().remove(follower, userId);

                return operations.exec();
            }
        });
    }

    //查询某用户关注的实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followee = RedisKeyUtil.getPrefixFollowee(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followee);
    }

    //统计实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId) {
        String follower = RedisKeyUtil.getPrefixFollower(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(follower);
    }

    //查询当前用户是否已关注该实体
    public boolean hasFollow(int userId, int entityType, int entityId) {
        String followee = RedisKeyUtil.getPrefixFollowee(userId, entityType);
        return redisTemplate.opsForZSet().score(followee, entityId) != null;
    }

    //查询某用户关注的人，返回时间和用户列表，支持分页
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followee = RedisKeyUtil.getPrefixFollowee(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followee, offset, limit + offset - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followee, targetId);
            map.put("followTime", new Date((score.longValue())));
            list.add(map);
        }
        return list;
    }

    //查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String follower = RedisKeyUtil.getPrefixFollower(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(follower, offset, limit + offset - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(follower, targetId);
            map.put("followTime", new Date((score.longValue())));
            list.add(map);
        }
        return list;
    }
}
