package com.wjk.halo.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.enums.MFAType;
import com.wjk.halo.model.params.LoginParam;
import com.wjk.halo.security.context.SecurityContextHolder;
import com.wjk.halo.security.token.AuthToken;
import com.wjk.halo.security.util.SecurityUtils;
import com.wjk.halo.service.AdminService;
import com.wjk.halo.service.UserService;
import com.wjk.halo.utils.HaloUtils;
import com.wjk.halo.utils.TwoFactorAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final AbstractStringCacheStore cacheStore;

    public AdminServiceImpl(UserService userService, ApplicationEventPublisher eventPublisher, AbstractStringCacheStore cacheStore) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.cacheStore = cacheStore;
    }

    /**
     * 整个流程为
     *  先根据用户名或者邮箱查找数据库，然后判断数据库中是否有对应的用户，若无报错，若有
     *  判断身份是否已被停用
     *  再判断密码是否匹配
     *
     * @param loginParam
     * @return
     */
    @Override
    @NonNull
    public User authenticate(@NonNull LoginParam loginParam) {
        String username = loginParam.getUsername();

        String mismatchTip = "用户名或者密码不正确";

        final User user;

        try {
            user = Validator.isEmail(username) ?
                    userService.getByEmailOfNonNull(username) :
                    userService.getByUsernameOfNonNull(username);
        }catch (NotFoundException e){
            log.error("Failed to find user by name: " + username);
            eventPublisher.publishEvent(new LogEvent(this, loginParam.getUsername(), LogType.LOGIN_FAILED, loginParam.getUsername()));
            throw new BadRequestException(mismatchTip);
        }

        userService.mustNotExpire(user);

        if (!userService.passwordMatch(user, loginParam.getPassword())){
            eventPublisher.publishEvent(new LogEvent(this, loginParam.getUsername(), LogType.LOGIN_FAILED, loginParam.getUsername()));

            throw new BadRequestException(mismatchTip);

        }
        return user;

    }

    @Override
    @NonNull
    public AuthToken authCodeCheck(@NonNull final LoginParam loginParam) {
        //相当于precheck做的检查
        final User user = this.authenticate(loginParam);

        //判断是否使用验证码登陆
        if (MFAType.useMFA(user.getMfaType())){
            //两步验证码判断
            if (StrUtil.isBlank(loginParam.getAuthcode())){
                throw new BadRequestException("请输入两步验证码");
            }
            TwoFactorAuthUtils.validateTFACode(user.getMfaKey(), loginParam.getAuthcode());
        }

        if (SecurityContextHolder.getContext().isAuthenticated()){
            throw new BadRequestException("您已登陆，请不要重复登陆");
        }

        eventPublisher.publishEvent(new LogEvent(this, user.getUsername(), LogType.LOGIN_IN, user.getNickname()));
        //生成token
        return buildAuthToken(user);
    }

    @NonNull
    private AuthToken buildAuthToken(@NonNull User user){
        AuthToken token = new AuthToken();

        token.setAccessToken(HaloUtils.randomUUIDWithoutDash());
        token.setExpiredIn(ACCESS_TOKEN_EXPIRED_SECONDS);
        token.setRefreshToken(HaloUtils.randomUUIDWithoutDash());
        //将token信息放入缓存，其中key为指定前缀+用户id
        cacheStore.putAny(SecurityUtils.buildAccessTokenKey(user), token.getAccessToken(), ACCESS_TOKEN_EXPIRED_SECONDS, TimeUnit.SECONDS);
        cacheStore.putAny(SecurityUtils.buildRefreshTokenKey(user), token.getRefreshToken(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);
        cacheStore.putAny(SecurityUtils.buildTokenAccessKey(token.getAccessToken()), user.getId(), ACCESS_TOKEN_EXPIRED_SECONDS, TimeUnit.SECONDS);
        cacheStore.putAny(SecurityUtils.buildTokenRefreshKey(token.getRefreshToken()),user.getId(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);

        return token;

    }

}
