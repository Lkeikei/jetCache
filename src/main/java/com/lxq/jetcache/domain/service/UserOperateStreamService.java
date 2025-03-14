package com.lxq.jetcache.domain.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxq.jetcache.domain.constant.UserOperateTypeEnum;
import com.lxq.jetcache.domain.entity.User;
import com.lxq.jetcache.domain.entity.UserOperateStream;
import com.lxq.jetcache.infrastructure.mapper.UserOperateStreamMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserOperateStreamService extends ServiceImpl<UserOperateStreamMapper, UserOperateStream> {


    public Long insertStream(User user, UserOperateTypeEnum type) {
        UserOperateStream stream = new UserOperateStream();
        stream.setUserId(String.valueOf(user.getId()));
        stream.setOperateTime(new Date());
        stream.setType(type.name());
        stream.setParam(JSON.toJSONString(user));
        boolean result = save(stream);
        if (result) {
            return stream.getId();
        }
        return null;
    }
}
