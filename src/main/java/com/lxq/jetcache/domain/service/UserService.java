package com.lxq.jetcache.domain.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxq.jetcache.api.constant.UserOperateTypeEnum;
import com.lxq.jetcache.api.request.UserModifyRequest;
import com.lxq.jetcache.api.response.UserOperatorResponse;
import com.lxq.jetcache.domain.entity.User;
import com.lxq.jetcache.infrastructure.exception.UserErrorCode;
import com.lxq.jetcache.infrastructure.mapper.UserMapper;
import com.lxq.jetcache.web.base.exception.BizException;
import com.lxq.jetcache.web.base.exception.RepoErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.lxq.jetcache.infrastructure.exception.UserErrorCode.DUPLICATE_TELEPHONE_NUMBER;
import static com.lxq.jetcache.infrastructure.exception.UserErrorCode.NICK_NAME_EXIST;
import static com.lxq.jetcache.infrastructure.exception.UserErrorCode.USER_NOT_EXIST;
import static com.lxq.jetcache.infrastructure.exception.UserErrorCode.USER_STATUS_CANT_OPERATE;

@Service
public class UserService extends ServiceImpl<UserMapper, User> implements InitializingBean {

    private static final String DEFAULT_NICK_NAME_PREFIX = "藏家_";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserOperateStreamService userOperateStreamService;

    //用户名布隆过滤器
    private RBloomFilter<String> nickNameBloomFilter;


    @Transactional(rollbackFor = Exception.class)
    public UserOperatorResponse register(String telephone, String inviteCode) {
        String defaultNickName;
        String randomString;
        do {
            randomString = RandomUtil.randomString(6).toUpperCase();
            //前缀 + 6位随机数 + 手机号后四位
            defaultNickName = DEFAULT_NICK_NAME_PREFIX + randomString + telephone.substring(7, 11);
        } while (nickNameExist(defaultNickName) /*|| inviteCodeExist(randomString)*/);

        String inviterId = null;
        if (StringUtils.isNotBlank(inviteCode)) {
            User inviter = userMapper.findByInviteCode(inviteCode);
            if (inviter != null) {
                inviterId = inviter.getId().toString();
            }
        }

        User user = register(telephone, defaultNickName, telephone, randomString, inviterId);
        Assert.notNull(user, UserErrorCode.USER_OPERATE_FAILED.getCode());

        addNickName(defaultNickName);
//        addInviteCode(randomString);
//        updateInviteRank(inviterId);
//        updateUserCache(user.getId().toString(), user);

        //加入流水
        long streamResult = userOperateStreamService.insertStream(user, UserOperateTypeEnum.REGISTER);
        Assert.notNull(streamResult, () -> new BizException(RepoErrorCode.UPDATE_FAILED));

        UserOperatorResponse userOperatorResponse = new UserOperatorResponse();
        userOperatorResponse.setSuccess(true);

        return userOperatorResponse;
    }


    /**
     * 更新用户信息
     *
     * @param userModifyRequest
     * @return
     */
    @Transactional
    @CacheInvalidate(name = ":user:cache:id:", key = "#userModifyRequest.userId")
    public UserOperatorResponse modify(UserModifyRequest userModifyRequest) {
        UserOperatorResponse userOperatorResponse = new UserOperatorResponse();
        User user = userMapper.findById(userModifyRequest.getUserId());
        Assert.notNull(user, () -> new BizException(USER_NOT_EXIST));
        Assert.isTrue(user.canModifyInfo(), () -> new BizException(USER_STATUS_CANT_OPERATE));

        if (StringUtils.isNotBlank(userModifyRequest.getNickName()) && nickNameExist(userModifyRequest.getNickName())) {
            throw new BizException(NICK_NAME_EXIST);
        }
        BeanUtils.copyProperties(userModifyRequest, user);

        if (StringUtils.isNotBlank(userModifyRequest.getPassword())) {
            user.setPasswordHash(DigestUtil.md5Hex(userModifyRequest.getPassword()));
        }
        if (updateById(user)) {
            //加入流水
            long streamResult = userOperateStreamService.insertStream(user, UserOperateTypeEnum.MODIFY);
            Assert.notNull(streamResult, () -> new BizException(RepoErrorCode.UPDATE_FAILED));
            addNickName(userModifyRequest.getNickName());
            userOperatorResponse.setSuccess(true);

            return userOperatorResponse;
        }
        userOperatorResponse.setSuccess(false);
        userOperatorResponse.setResponseCode(UserErrorCode.USER_OPERATE_FAILED.getCode());
        userOperatorResponse.setResponseCode(UserErrorCode.USER_OPERATE_FAILED.getMessage());

        return userOperatorResponse;
    }

    private User register(String telephone, String nickName, String password, String inviteCode, String inviterId) {
        if (userMapper.findByTelephone(telephone) != null) {
            throw new BizException(DUPLICATE_TELEPHONE_NUMBER);
        }

        User user = new User();
        user.register(telephone, nickName, password, inviteCode, inviterId);
        return save(user) ? user : null;
    }


    public boolean nickNameExist(String nickName) {
        //如果布隆过滤器中存在，再进行数据库二次判断
        if (this.nickNameBloomFilter != null && this.nickNameBloomFilter.contains(nickName)) {
            return userMapper.findByNickname(nickName) != null;
        }

        return false;
    }

    private boolean addNickName(String nickName) {
        return this.nickNameBloomFilter != null && this.nickNameBloomFilter.add(nickName);
    }

    @Override
    public void afterPropertiesSet() {
        this.nickNameBloomFilter = redissonClient.getBloomFilter("nickName");
        if (nickNameBloomFilter != null && !nickNameBloomFilter.isExists()) {
            this.nickNameBloomFilter.tryInit(100000L, 0.01);
        }
    }

    @Cached(name = ":user:cache:id:", cacheType = CacheType.BOTH, key = "#userId", cacheNullValue = true)
    @CacheRefresh(refresh = 60, timeUnit = TimeUnit.MINUTES)
    public User findById(Long userId) {
        return userMapper.findById(userId);
    }
}
