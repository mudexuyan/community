package com.nowcode.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
    //'*"表示返回值 om.nowcode.community.service,包名.所有类.所有方法 (..)表示所有参数
    //返回值 包名.所有类.所有方法(所有参数)
    @Pointcut("execution(* com.nowcode.community.service.*.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before() {
        System.out.println("before");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("after");
    }

    @AfterReturning("pointcut()")
    public void afterRunning() {
        System.out.println("afterRunning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");
        Object object = joinPoint.proceed();
        System.out.println("around after");
        return object;
    }

}
