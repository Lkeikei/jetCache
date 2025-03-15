package com.lxq.jetcache.api.auth.param;

import com.lxq.jetcache.param.RegisterParam;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginParam extends RegisterParam {

    /**
     * 记住我
     */
    private Boolean rememberMe;
}