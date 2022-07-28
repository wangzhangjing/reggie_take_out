package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/*
* 检查用户是否登入
*
*
* */
@WebFilter(filterName ="loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;
        //1获取本次请求uri
        String requestURI =request.getRequestURI();
         log.info("拦截到请求: {}",request.getRequestURI());
        //定义不需要处理的请求路径
        String[]urls =new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/*",
                "/user/sendMsg",
                "/user/login",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };
        //2判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        //3不需要处理直接放行
        if (check){
            log.info("本次请求不需要处理，",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //4-1判断登入状态，如果已经登入直接放行
        if (request.getSession().getAttribute("employee") !=null){
            log.info("用户已登入，用户id为 {}",request.getSession().getAttribute("employee"));

            long id = Thread.currentThread().getId();
            log.info("线程id为：{}",id);
            //在线程中存储id
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setcurrId(empId);
            filterChain.doFilter(request,response);
            return;
        }
        //4.2判断登入状态，如果已经登入直接放行
        if (request.getSession().getAttribute("user") !=null){
            log.info("用户已登入，用户id为 {}",request.getSession().getAttribute("user"));

            long id = Thread.currentThread().getId();
            log.info("线程id为：{}",id);
            //在线程中存储id
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setcurrId(userId);
            filterChain.doFilter(request,response);
            return;
        }
        log.error("用户未登入");
        //5如果未登入则返回未登入结果,通过输出流方式向客户端页面响应数据

        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

        log.info("拦截到请求: {}",request.getRequestURI());
        return;
    }
    //路径匹配，检查当前请求是否需要放行
    public boolean check(String [] urls,String requestURI){
        for (String url:urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
