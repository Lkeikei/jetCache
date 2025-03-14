package com.lxq.jetcache.controller;

import com.lxq.jetcache.domain.service.UserService;
import com.lxq.jetcache.web.vo.Result;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;


    @GetMapping("/testGet")
    public Result<String> test() {

        return Result.success("success");
    }

}
