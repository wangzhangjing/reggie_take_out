package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        //新增菜品
        log.info("disidto:{}",dishDto);
        dishService.saveWithFlavor(dishDto);

        return R.success("添加成功");
    }
    @GetMapping("/page")
    //菜品信息分页查询
    public R<Page> page(int page,int pageSize,String name){

        Page<Dish> pageInfo =new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage =new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name!=null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);//所有属性拷贝
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category!=null){
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
        }
            return dishDto;
        }).collect(Collectors.toList());


        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }
    //根据id查询菜品信息，口味信息
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto= dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        //修改菜品
        log.info("disidto:{}",dishDto);
        dishService.updateWithFlavor(dishDto);

        //修改菜品后把所以缓存删除
//        Set keys = redisTemplate.keys("dish_*");
        //精确清理
        String key ="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("修改成功");
    }

    //根据id删除分类
//    @DeleteMapping
//    public R<String> delete(Long ids){
//        log.info("删除分类：id为{}",ids);
//        dishService.removeWithFlavor(ids);
//        return R.success("删除菜品成功");
//    }
//    @PostMapping("/status/{status}")
//    public R<Dish> stoporit(Long ids,@PathVariable int status){
//    log.info("ids:{}",ids);
//    Dish d=dishService.getById(ids);
//    d.setStatus(status);
////    d.setId(ids);
////    if(d.getStatus()==0) {
////        d.setStatus(1);
////    }else{
////        d.setStatus(0);
////    }
//    dishService.updateById(d);
//        return R.success(d);
//    }
    //根据id删除分类
    @DeleteMapping
    public R<String> deletes(String ids){
        log.info("删除分类：id为{}",ids);
        List idst = Arrays.asList(ids.split(","));
        idst.forEach(idss ->{
            //转成Long
            Long id =Long.parseLong((String) idss);
            //调用删除方法
            dishService.removeWithFlavor(id);
        });
//        dishService.removeWithFlavors(ids);
//        return R.success("删除菜品成功");
        return R.success("删除菜品成功");
    }
    @PostMapping("/status/{status}")
    public R<String> stoporit(String ids,@PathVariable int status){
        log.info("ids:{}",ids);

        List idst = Arrays.asList(ids.split(","));
        idst.forEach(idss ->{
            //转成Long
            Long id =Long.parseLong((String) idss);
            //调用修改方法
            Dish d=dishService.getById(id);
            d.setStatus(status);
            dishService.updateById(d);

        });

        return R.success("修改成功");
    }
    //根据条件查询菜品数据
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        //查询条件
//        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        //查看status是否是启用状态
//        queryWrapper.eq(Dish::getStatus,1);
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }
    //根据条件查询菜品数据22222222
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtos=null;
        //动态构造
        String key="dish_"+ dish.getCategoryId()+"_"+dish.getStatus();//dish_15346457548485_1
        //先从redis中获取缓存数据
        dishDtos = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，直接返回，无需操作数据库
        if (dishDtos!=null){
            return R.success(dishDtos);
        }

        //查询条件
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //查看status是否是启用状态
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        dishDtos=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);//所有属性拷贝
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品id
            Long dishid = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1 =new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,dishid);
            //select * from dishflavor where dish-id=?
            List<DishFlavor> list1 = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(list1);
            return dishDto;
        }).collect(Collectors.toList());
//如果不存在，查询数据库，把查询的数据缓存到redis

        redisTemplate.opsForValue().set(key,dishDtos,60, TimeUnit.MINUTES);


        return R.success(dishDtos);
    }
}
