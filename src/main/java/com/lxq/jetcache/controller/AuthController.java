package com.lxq.jetcache.controller;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.lxq.jetcache.api.auth.param.LoginParam;
import com.lxq.jetcache.api.auth.vo.LoginVO;
import com.lxq.jetcache.api.data.UserInfo;
import com.lxq.jetcache.api.request.UserQueryRequest;
import com.lxq.jetcache.web.vo.Result;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: 娄须强
 * @CreateTime: 2025-03-15  17:37
 */
@RestController
@RequestMapping("/")
public class AuthController {


    /**
     * 登录方法
     *
     * @param loginParam 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginParam loginParam) {
        //fixme 为了方便，暂时直接跳过
//        if (!ROOT_CAPTCHA.equals(loginParam.getCaptcha())) {
//            //验证码校验
//            String cachedCode = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + loginParam.getTelephone());
//            if (!StringUtils.equalsIgnoreCase(cachedCode, loginParam.getCaptcha())) {
//                throw new AuthException(VERIFICATION_CODE_WRONG);
//            }
//        }

        //判断是注册还是登陆
        //查询用户信息
        UserQueryRequest userQueryRequest = new UserQueryRequest(loginParam.getTelephone());
        UserQueryResponse<UserInfo> userQueryResponse = userFacadeService.query(userQueryRequest);
        UserInfo userInfo = userQueryResponse.getData();
        if (userInfo == null) {
            //需要注册
            UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
            userRegisterRequest.setTelephone(loginParam.getTelephone());
            userRegisterRequest.setInviteCode(loginParam.getInviteCode());

            UserOperatorResponse response = userFacadeService.register(userRegisterRequest);
            if (response.getSuccess()) {
                userQueryResponse = userFacadeService.query(userQueryRequest);
                userInfo = userQueryResponse.getData();
                StpUtil.login(userInfo.getUserId(), new SaLoginModel().setIsLastingCookie(loginParam.getRememberMe())
                        .setTimeout(DEFAULT_LOGIN_SESSION_TIMEOUT));
                StpUtil.getSession().set(userInfo.getUserId().toString(), userInfo);
                LoginVO loginVO = new LoginVO(userInfo);
                return Result.success(loginVO);
            }

            return Result.error(response.getResponseCode(), response.getResponseMessage());
        } else {
            //登录
            StpUtil.login(userInfo.getUserId(), new SaLoginModel().setIsLastingCookie(loginParam.getRememberMe())
                    .setTimeout(DEFAULT_LOGIN_SESSION_TIMEOUT));
            StpUtil.getSession().set(userInfo.getUserId().toString(), userInfo);
            LoginVO loginVO = new LoginVO(userInfo);
            return Result.success(loginVO);
        }
    }
}
