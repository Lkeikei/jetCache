package com.lxq.jetcache.domain.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxq.jetcache.api.response.UserOperatorResponse;
import com.lxq.jetcache.domain.constant.UserOperateTypeEnum;
import com.lxq.jetcache.domain.entity.User;
import com.lxq.jetcache.infrastructure.exception.UserErrorCode;
import com.lxq.jetcache.infrastructure.mapper.UserMapper;
import com.lxq.jetcache.web.base.exception.BizException;
import com.lxq.jetcache.web.base.exception.RepoErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.lxq.jetcache.infrastructure.exception.UserErrorCode.DUPLICATE_TELEPHONE_NUMBER;

@Service
public class UserService extends ServiceImpl<UserMapper, User> implements InitializingBean {

    private static final String DEFAULT_NICK_NAME_PREFIX = "藏家_";

    @Autowired
    private UserMapper userMapper;


    @Autowired
    private UserOperateStreamService userOperateStreamService;

    //用户名布隆过滤器
    private RBloomFilter<String> nickNameBloomFilter;


    @Transactional
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

    }
}
