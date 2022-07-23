package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    /*
     * 员工登入
     * */
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

      // 1将页面提交的密码进行MD5加密处理
        String passeord = employee.getPassword();
        passeord = DigestUtils.md5DigestAsHex(passeord.getBytes());
      //2根据页面提交用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3如果没有查询到返回失败
        if (emp==null){
            return R.error("登入失败");
        }
        // 4如果密码不匹配返回失败
        if (!emp.getPassword().equals(passeord)){
            return R.error("登入失败");
        }
        // 5查看员工状态，如果为已禁用则返回员工已禁用结果
        if (emp.getStatus()==0){
            return R.error("账号已禁用");
        }
        // 6登入成功将员工id存入Session并且返回成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    /*
    *员工退出
    * */

        @PostMapping("/logout")
        public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");

        return R.success("混子，退出成功");
        }
        @PostMapping
        //新增员工
        public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
           log.info("员工信息：{}" ,employee.toString());
           //设置初始密码需要MD5加密处理
           employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//           employee.setCreateTime(LocalDateTime.now());
//           employee.setUpdateTime(LocalDateTime.now());
//
//            Long empId = (Long) request.getSession().getAttribute("employee");
//            employee.setCreateUser(empId);
//            employee.setUpdateUser(empId);

            employeeService.save(employee);

            return R.success("新增员工成功");
        }
        @GetMapping("/page")
        public R<Page> page(int page,int pageSize,String name){
            log.info("page ={},pageSize = {},name ={}",page,pageSize,name);

            //构造分页构造器
            Page pageInfo =new Page(page,pageSize);

            //构造条件构造器
            LambdaQueryWrapper<Employee> queryWrapper =new LambdaQueryWrapper();
            //添加过滤添加
            queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
            //添加排序条件
            queryWrapper.orderByDesc(Employee::getUpdateTime);
            //执行查询
            employeeService.page(pageInfo,queryWrapper);

            return R.success(pageInfo);
        }
        @PutMapping
        public R<String> updata(HttpServletRequest request ,@RequestBody Employee employee){
            log.info(employee.toString());
//            Long employee1 =(Long) request.getSession().getAttribute("employee");
//            employee.setUpdateTime(LocalDateTime.now());
//            employee.setUpdateUser(employee1);

            long id = Thread.currentThread().getId();
            log.info("线程id为：{}",id);

            employeeService.updateById(employee);
            return R.success("员工信息修改成功");
        }
        //根据id查询员工信息
        @GetMapping("/{id}")
        public R<Employee> getById(@PathVariable Long id){
            log.info("根据id查员工信息");
            Employee employee1 = employeeService.getById(id);
            if (employee1!=null){
            return R.success(employee1);
            }
            return R.error("没有查询到员工信息");
        }
}
