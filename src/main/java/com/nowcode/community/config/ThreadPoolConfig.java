package com.nowcode.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling//用于spring线程池
@EnableAsync
public class ThreadPoolConfig {
}
