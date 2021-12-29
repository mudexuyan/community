package com.nowcode.community.service;

import com.nowcode.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    //将指定ip计入UV
    public void recordUV(String ip) {
        String key = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(key, ip);
    }

    //统计指定日期范围内的uv
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("uv日期不能为空");
        }
        //整理日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            //加一天
            calendar.add(Calendar.DATE, 1);
        }
        //合并数据
        String key = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        long size = redisTemplate.opsForHyperLogLog().union(key, keyList.toArray());
        return size;
    }

    //将指定用户计入dau
    public void recordDAU(int userId) {
        String key = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(key, userId, true);
    }

    //统计指定日期范围内的DAU
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("uv日期不能为空");
        }
        //整理日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            //加一天
            calendar.add(Calendar.DATE, 1);
        }
        //合并数据
        String key = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        key.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(key.getBytes());
            }
        });
    }
}
