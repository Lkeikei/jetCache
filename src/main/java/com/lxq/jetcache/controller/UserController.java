package com.lxq.jetcache.controller;

import com.lxq.jetcache.web.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {


    @GetMapping("/testGet")
    public Result<String> test() {

        return Result.success("success");
    }

}
