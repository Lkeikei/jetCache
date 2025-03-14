package com.lxq.jetcache.domain.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxq.jetcache.domain.entity.User;
import com.lxq.jetcache.infrastructure.mapper.UserMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> implements InitializingBean {




    @Override
    public void afterPropertiesSet() {

    }
}
