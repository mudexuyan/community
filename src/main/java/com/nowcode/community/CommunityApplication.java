package com.nowcode.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CommunityApplication {
//    @PostConstruct
//    public void init() {
//        //解决netty启动冲突问题,netty4utils
//        System.setProperty("es.set.netty.runtime.available.processors", "false");
//    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
