package com.yizong.cleanupsystem.controller;

import com.yizong.cleanupsystem.common.Result;
import com.yizong.cleanupsystem.entity.Admin;
import com.yizong.cleanupsystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<Admin> login(@RequestBody Admin admin) {
        Admin foundAdmin = adminService.login(admin.getUsername(), admin.getPassword());
        if (foundAdmin != null && foundAdmin.getPassword().equals(admin.getPassword())) {
            return Result.success("登录成功", foundAdmin);
        }
        return Result.error("用户名或密码错误");
    }
}
