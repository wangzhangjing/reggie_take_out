package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品同时插入对应的口味数据 dish dish-flavor
    public void saveWithFlavor(DishDto dishDto);
    //根据id来查询对应的菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id);
     //更新菜品信息和口味信息
    public void updateWithFlavor(DishDto dishDto);

    public void removeWithFlavor(Long ids);

//    public void removeWithFlavors(Long[] ids);

}
