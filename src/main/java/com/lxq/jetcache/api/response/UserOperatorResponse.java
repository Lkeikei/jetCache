package com.lxq.jetcache.api.response;

import com.lxq.jetcache.api.data.UserInfo;
import com.lxq.jetcache.web.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOperatorResponse extends BaseResponse {

    private UserInfo user;
}