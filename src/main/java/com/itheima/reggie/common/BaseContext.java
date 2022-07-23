package com.itheima.reggie.common;
/*
* 基于ThreadLOcal封装工具类，用户报错和获取当前登入信息
* 范围一个线程
*
* */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal =new ThreadLocal<>();

    public static void setcurrId(Long id){
        threadLocal.set(id);
    }

    public static Long getcurrId(){
        return threadLocal.get();
    }
}
