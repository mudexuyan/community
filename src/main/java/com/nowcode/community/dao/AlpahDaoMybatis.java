package com.nowcode.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlpahDaoMybatis implements AlphaDao {
    @Override
    public String select() {
        return "Mybatis";
    }
}
