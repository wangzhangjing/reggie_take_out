package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    //增加套餐
    public void saveWhitDish(SetmealDto setmealDto);
    //删除套餐同时删除套餐和菜品关联数据
    public void removeWithDish(List<Long> ids);
}
