package com.nowcode.community.service;

import com.nowcode.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    //点赞，谁给哪条帖子或者n哪条评论点赞
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getPrefixUserLike(entityUserId);
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();
                if (isMember) {
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    //查询实体点赞数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String key = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String key = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
        boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        if (isMember) {
            return 1;
        } else {
            return 0;
        }
    }

    //查询某个用户获得的数量
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getPrefixUserLike(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
