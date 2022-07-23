package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    //新增菜品同时保存对应口味数据
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到dish表中
        this.save(dishDto);
        //保存完后会创建id，我们获取id
        Long dishId = dishDto.getId();
        //数据处理
        List<DishFlavor> flavors=dishDto.getFlavors();
        flavors=flavors.stream().map((item)-> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表diah-flavor中
        dishFlavorService.saveBatch(flavors);
    }

    //根据id来查询对应的菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息从dish查询
        Dish dish = this.getById(id);
        DishDto dishDto=new DishDto();

        BeanUtils.copyProperties(dish,dishDto);
        //查询当前菜品口味信息从dishflavor查询
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);


        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表信息
        this.updateById(dishDto);

        //先清理菜品对应口味信息数据delete
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加提交过来的口味数据insert
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors=flavors.stream().map((item)-> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void removeWithFlavor(Long ids) {
        this.removeById(ids);
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);
    }

//    @Override
//    public void removeWithFlavors(Long[] ids) {
//        this.removeByIds(ids);
//        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper();
//        queryWrapper.eq(DishFlavor::getDishId,ids);
//        dishFlavorService.remove(queryWrapper);
//    }
}
