package com.lxq.jetcache.api.request.condition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserPhoneQueryCondition implements UserQueryCondition {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户手机号
     */
    private String telephone;
}