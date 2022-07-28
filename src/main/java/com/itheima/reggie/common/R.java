package com.itheima.reggie.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@ApiModel("返回结果")
public class R<T> implements Serializable {//implements Serializable可以让r的返回值进行缓存，实现序列号
    @ApiModelProperty("编码1 成功 0 和其他失败")
    private Integer code;//编码1 成功 0 和其他失败

    @ApiModelProperty("错误信息")
    private String msg;//错误信息

    @ApiModelProperty("数据")
    private T data;//数据

    @ApiModelProperty("动态数据败")
    private Map map=new HashMap();//动态数据

    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
