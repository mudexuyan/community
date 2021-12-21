package com.nowcode.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString() {
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash() {
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");
        redisTemplate.opsForHash().put(redisKey, "age", "20");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "age"));
    }

    @Test
    public void testList() {
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));

    }

    @Test
    public void testSet() {
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "刘备", "关羽", "张飞", "赵云", "黄忠");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testZset() {
        String redisKey = "test:students";
        redisTemplate.opsForZSet().add(redisKey, "唐僧", 100);
        redisTemplate.opsForZSet().add(redisKey, "悟空", 80);
        redisTemplate.opsForZSet().add(redisKey, "八戒", 80);
        redisTemplate.opsForZSet().add(redisKey, "沙和尚", 80);
        redisTemplate.opsForZSet().add(redisKey, "白龙马", 60);

        System.out.println(redisTemplate.opsForZSet().size(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "悟空"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "悟空"));
        //默认从小到大
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 4));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 4));
    }

    @Test
    public void testKey() {
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    //多次访问同一个key
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    //由于redis事务，是将多个请求送到一个队列中，之后批量发送，如果队列中有查询请求就会造成不一致。只能在事务前或事务后查询
    //所以需要编程式处理事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String key = "test:tx";
                //启用事务
                operations.multi();

                operations.opsForSet().add(key,"刘备");
                operations.opsForSet().add(key,"关羽");
                operations.opsForSet().add(key,"张飞");
                System.out.println(operations.opsForSet().members(key));

                //提交事务
                return operations.exec();
            }
        });
        System.out.println(obj);
    }
}
