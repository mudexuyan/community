package com.nowcode.community.Controller.interceptor;

import com.nowcode.community.entity.LoginTicket;
import com.nowcode.community.entity.User;
import com.nowcode.community.service.UserService;
import com.nowcode.community.util.CookieUtil;
import com.nowcode.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;
    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取session凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            //查询ticket信息，包含了userId
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserById(loginTicket.getUserId());
                //多线程存储对象
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
