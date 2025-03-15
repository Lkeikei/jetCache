package com.lxq.jetcache.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lxq.jetcache.api.data.UserInfo;
import com.lxq.jetcache.api.request.UserModifyRequest;
import com.lxq.jetcache.api.response.UserOperatorResponse;
import com.lxq.jetcache.domain.convertor.UserConvertor;
import com.lxq.jetcache.domain.entity.User;
import com.lxq.jetcache.domain.service.UserService;
import com.lxq.jetcache.param.RegisterParam;
import com.lxq.jetcache.param.UserModifyParam;
import com.lxq.jetcache.web.base.exception.BizException;
import com.lxq.jetcache.web.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.lxq.jetcache.infrastructure.exception.UserErrorCode.USER_NOT_EXIST;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserService userService;


    @GetMapping("/testGet")
    public Result<String> test() {

        return Result.success("success");
    }

    @GetMapping("/getUserInfo")
    public Result<UserInfo> getUserInfo() {
        String userId = (String) StpUtil.getLoginId();
        User user = userService.findById(Long.valueOf(userId));

        if (user == null) {
            throw new BizException(USER_NOT_EXIST);
        }
        return Result.success(UserConvertor.INSTANCE.mapToVo(user));
    }


    @PutMapping("/modifyNickName")
    public Result<Boolean> modifyNickName(@Valid @RequestBody UserModifyParam userModifyParam) {
        String userId = (String) StpUtil.getLoginId();

        //修改信息
        UserModifyRequest userModifyRequest = new UserModifyRequest();
        userModifyRequest.setUserId(Long.valueOf(userId));
        userModifyRequest.setNickName(userModifyParam.getNickName());

        Boolean registerResult = userService.modify(userModifyRequest).getSuccess();
        return Result.success(registerResult);
    }

    @PostMapping("/register")
    public Result<Boolean> register(@Valid @RequestBody RegisterParam registerParam) {

        //验证码校验
//        String cachedCode = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + registerParam.getTelephone());
//        if (!StringUtils.equalsIgnoreCase(cachedCode, registerParam.getCaptcha())) {
//            throw new BizException(VERIFICATION_CODE_WRONG);
//        }

        //注册
//        UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
//        userRegisterRequest.setTelephone(registerParam.getTelephone());
//        userRegisterRequest.setInviteCode(registerParam.getInviteCode());

        UserOperatorResponse registerResult = userService.register(registerParam.getTelephone(), registerParam.getInviteCode());
        if (registerResult.getSuccess()) {
            return Result.success(true);
        }
        return Result.error(registerResult.getResponseCode(), registerResult.getResponseMessage());
    }

}
