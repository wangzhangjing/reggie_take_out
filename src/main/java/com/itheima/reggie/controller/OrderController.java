package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("orders={}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }
    @GetMapping("/page")
    public R<Page> ddb(int page, int pageSize, String number, LocalDateTime beginTime,LocalDateTime endTime){

        Page<Orders> pageInfo =new Page<>(page,pageSize);
        Long getcurrId = BaseContext.getcurrId();
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();

        //添加订单号查询情况
        queryWrapper.eq(number!=null,Orders::getNumber,number);
        //从开始时间到订单完成时间的查询
        queryWrapper.gt(beginTime!=null,Orders::getOrderTime,beginTime);
        //从付款时间到最后时间查询
        queryWrapper.lt(endTime!=null,Orders::getCheckoutTime,endTime);
        //排序根据时间
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo, queryWrapper);
        //对象拷贝
        Page<OrdersDto> www =new Page<>();
        BeanUtils.copyProperties(pageInfo,www);

        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> list=records.stream().map((item)->{
            OrdersDto orders=new OrdersDto();
            BeanUtils.copyProperties(item,orders);//所有属性拷贝
            orders.setUserName(String.valueOf(getcurrId));
            log.info("orders={}",orders);
            return orders;
        }).collect(Collectors.toList());

        www.setRecords(list);

        return R.success(www);
    }
    @PutMapping
    public R<String> paison(@RequestBody Orders orders){
        LambdaUpdateWrapper<Orders> queryWrapper=new LambdaUpdateWrapper<>();
        queryWrapper.eq(Orders::getId,orders.getId());
        queryWrapper.set(Orders::getStatus,orders.getStatus()+1);
        orderService.update(queryWrapper);
        return R.success("派送成功");
    }
    @GetMapping("/userPage")
    public R<Page> zdcx(int page, int pageSize){
        Page<Orders> pageInfo =new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> wzj=new LambdaQueryWrapper<>();
        wzj.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,wzj);
    return R.success(pageInfo);
    }
    @PostMapping("/again")
    public R<String> again(Long id){
        LambdaQueryWrapper<OrderDetail> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getId,id);
        List<OrderDetail> list = orderDetailService.list(queryWrapper);
        return null;
    }
}
