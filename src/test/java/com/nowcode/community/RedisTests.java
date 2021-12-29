package com.nowcode.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
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

                operations.opsForSet().add(key, "刘备");
                operations.opsForSet().add(key, "关羽");
                operations.opsForSet().add(key, "张飞");
                System.out.println(operations.opsForSet().members(key));

                //提交事务
                return operations.exec();
            }
        });
        System.out.println(obj);
    }

    //hyperloglog
    //统计20万个重复数据的独立总数，去重后的数量
    @Test
    public void testHyperLoglog() {
        String redisKey = "test:hll:01";
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 1; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }
        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);

        System.out.println(size);
    }

    //将三组数据合并，再统计合并后的重复数据的独立总数
    @Test
    public void testUnion() {
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        String redisKey3 = "test:hll:03";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }
        String redisKey4 = "test:hll:04";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }
        String unionKey = "test:hll:union";
        Long size1 = redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);
        System.out.println(size1);//19833
        Long size2 = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size2);//19833
    }

    /*
     * bitmap，统计一组数据的布尔值
     */
    @Test
    public void testBitMap() {
        String key = "test:bm:01";
        //记录
        redisTemplate.opsForValue().setBit(key, 1, true);
        redisTemplate.opsForValue().setBit(key, 4, true);
        redisTemplate.opsForValue().setBit(key, 7, true);
        //查询
        System.out.println(redisTemplate.opsForValue().getBit(key, 0));
        System.out.println(redisTemplate.opsForValue().getBit(key, 1));
        System.out.println(redisTemplate.opsForValue().getBit(key, 2));
        //统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(key.getBytes());
            }
        });
        System.out.println(obj);
    }

    /*
     *统计三组数据的布尔值，并对三组数据做or运算
     */
    @Test
    public void testOperation() {
        String key2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(key2, 0, true);
        redisTemplate.opsForValue().setBit(key2, 1, true);
        redisTemplate.opsForValue().setBit(key2, 2, true);

        String key3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(key3, 2, true);
        redisTemplate.opsForValue().setBit(key3, 3, true);
        redisTemplate.opsForValue().setBit(key3, 4, true);

        String key4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(key4, 5, true);
        redisTemplate.opsForValue().setBit(key4, 6, true);
        redisTemplate.opsForValue().setBit(key4, 4, true);

        String redisKey = "test:bm:04";
        Object object = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), key2.getBytes(), key3.getBytes(), key4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(object);
    }
}
