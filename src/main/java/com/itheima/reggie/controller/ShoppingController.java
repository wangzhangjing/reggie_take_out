package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Spliterator;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("shoppingCart={}",shoppingCart);

        //设置用户id，指定当前是哪个用户的购物车数据
        Long cid = BaseContext.getcurrId();
        shoppingCart.setUserId(cid);
        //查询当前菜品或者套餐是否在购物车中
        Long dishid = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,cid);
        if (dishid!=null){
            //添加的购物车是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishid);
        }else {
            //添加的购物车是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //select * from shppingcat where user-id=? and dish-id=?/setmeal-id=?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        //如果已经存在在原理的数量上加一
        if (cartServiceOne!=null){
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //如果不存在数量默认就是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne=shoppingCart;
        }

        return R.success(cartServiceOne);
    }
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getcurrId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getcurrId());
        shoppingCartService.remove(queryWrapper);
     return R.success("清空购物车成功");
    }
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        Long aLong = BaseContext.getcurrId();
        log.info("shoppingCart:{}",shoppingCart);
        Long dishid1 = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(ShoppingCart::getUserId,aLong);
        if (dishid1!=null){
            //添加的购物车是菜品
            queryWrapper2.eq(ShoppingCart::getDishId,dishid1);
        }else {
            //添加的购物车是套餐
            queryWrapper2.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart serviceOne = shoppingCartService.getOne(queryWrapper2);
        if (serviceOne.getNumber()>1){
            Integer number = serviceOne.getNumber();
            serviceOne.setNumber(number-1);
            shoppingCartService.updateById(serviceOne);
        }else {
            LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getcurrId());
            shoppingCartService.remove(queryWrapper);
            serviceOne=shoppingCart;
        }
        return R.success(serviceOne);
    }
}
