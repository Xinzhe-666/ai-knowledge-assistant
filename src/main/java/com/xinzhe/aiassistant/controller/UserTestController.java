package com.xinzhe.aiassistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinzhe.aiassistant.common.result.Result;
import com.xinzhe.aiassistant.entity.User;
import com.xinzhe.aiassistant.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户CRUD测试接口
 * 演示MyBatis-Plus的基础用法
 */
@RestController
@RequestMapping("/user/test")
public class UserTestController {

    // 注入UserService，Spring会自动把UserServiceImpl的对象注入进来
    @Autowired
    private UserService userService;

    /**
     * 1. 新增用户
     * 访问地址：POST http://localhost:8080/user/test/add
     * 请求体：{"username":"test","password":"123456","nickname":"测试用户"}
     */
    @PostMapping("/add")
    public Result<User> addUser(@RequestBody User user) {
        // 设置默认值
        user.setRole("user");
        user.setStatus(1);
        user.setDeleted(0); // 加上这一行！设置逻辑删除默认值为0
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 保存用户到数据库
        boolean success = userService.save(user);
        if (success) {
            return Result.success(user);
        } else {
            return Result.fail("新增用户失败");
        }
    }

    /**
     * 2. 根据ID查询用户
     * 访问地址：GET http://localhost:8080/user/test/get/1
     */
    @GetMapping("/get/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        // 根据ID查询，getById方法是IService提供的
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 3. 查询所有用户
     * 访问地址：GET http://localhost:8080/user/test/list
     */
    @GetMapping("/list")
    public Result<List<User>> getAllUsers() {
        // 查询所有用户，list方法是IService提供的
        List<User> userList = userService.list();
        return Result.success(userList);
    }

    /**
     * 4. 分页查询用户
     * 访问地址：GET http://localhost:8080/user/test/page?pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<Page<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        // 创建分页对象，参数：当前页，每页条数
        Page<User> page = new Page<>(pageNum, pageSize);
        // 分页查询，page方法是IService提供的
        Page<User> userPage = userService.page(page);
        return Result.success(userPage);
    }

    /**
     * 5. 根据ID更新用户
     * 访问地址：PUT http://localhost:8080/user/test/update
     * 请求体：{"id":1,"nickname":"修改后的昵称"}
     */
    @PutMapping("/update")
    public Result<User> updateUser(@RequestBody User user) {
        user.setUpdatedAt(LocalDateTime.now());
        // 根据ID更新，updateById方法是IService提供的
        boolean success = userService.updateById(user);
        if (success) {
            return Result.success(userService.getById(user.getId()));
        } else {
            return Result.fail("更新用户失败");
        }
    }

    /**
     * 6. 根据ID删除用户（逻辑删除）
     * 访问地址：DELETE http://localhost:8080/user/test/delete/1
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        // 根据ID删除，removeById方法是IService提供的
        // 因为配置了逻辑删除，所以这里是更新deleted字段为1，不是真的删除
        boolean success = userService.removeById(id);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("删除用户失败");
        }
    }

    /**
     * 7. 条件查询：根据用户名查询用户
     * 访问地址：GET http://localhost:8080/user/test/getByUsername?username=test
     */
    @GetMapping("/getByUsername")
    public Result<User> getUserByUsername(@RequestParam String username) {
        // Lambda查询构造器，用来构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 构建条件：username = 传入的用户名
        queryWrapper.eq(User::getUsername, username);
        // 根据条件查询一个用户，getOne方法是IService提供的
        User user = userService.getOne(queryWrapper);
        return Result.success(user);
    }
}