package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/*
* 自定义元数据处理器
*
*
* */
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公工字段自动填充[insert]");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getcurrId());
        metaObject.setValue("updateUser",BaseContext.getcurrId());

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公工字段自动填充[updata]");
        log.info(metaObject.toString());
        long id =Thread.currentThread().getId();
        log.info("当前线程为：{}",id);
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getcurrId());
    }
}
