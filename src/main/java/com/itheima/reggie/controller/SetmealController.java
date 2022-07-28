package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
//import com.sun.xml.internal.ws.server.ServerRtException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//套餐管理
@RestController
@RequestMapping("/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWhitDish(setmealDto);

        return R.success("新增套餐成功");
    }
    @GetMapping("/page")
    @ApiOperation(value = "套餐分页接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页显示条数",required = true),
            @ApiImplicitParam(name = "name",value = "套餐名称",required = true)
    })
    public R<Page> page(int page,int pageSize,String name){
        Page pageInfo =new Page(page,pageSize);

        Page dtopage =new Page(page,pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //根据name进行模糊查询
        queryWrapper.like(name!=null,Setmeal::getName,name);
        //排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtopage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list=records.stream().map((item)->{
            SetmealDto sw =new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,sw);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询数据对象
            Category byId = categoryService.getById(categoryId);
            if (byId!=null){
                String categoryName = byId.getName();
                sw.setCategoryName(categoryName);
            }
            return sw;
        }).collect(Collectors.toList());

        dtopage.setRecords(list);

        return R.success(dtopage);
    }
    //删除套餐
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }
    @PostMapping("/status/{status}")
    public R<String> stoporit(String ids,@PathVariable int status){
        log.info("ids:{}",ids);

        List idst = Arrays.asList(ids.split(","));
        idst.forEach(idss ->{
            //转成Long
            Long id =Long.parseLong((String) idss);
            //调用修改方法
            Setmeal d=setmealService.getById(id);
            d.setStatus(status);
            setmealService.updateById(d);

        });

        return R.success("修改成功");
    }
    //根据条件查询套餐数据
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId +'_'+ #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
      LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
      queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
      queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
      queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

       return R.success(list);
    }

}
