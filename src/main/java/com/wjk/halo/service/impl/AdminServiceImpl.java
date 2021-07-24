package com.wjk.halo.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.mail.MailService;
import com.wjk.halo.model.properties.EmailProperties;
import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.enums.MFAType;
import com.wjk.halo.model.params.LoginParam;
import com.wjk.halo.model.params.ResetPasswordParam;
import com.wjk.halo.security.authentication.Authentication;
import com.wjk.halo.security.context.SecurityContextHolder;
import com.wjk.halo.security.token.AuthToken;
import com.wjk.halo.security.util.SecurityUtils;
import com.wjk.halo.service.AdminService;
import com.wjk.halo.service.OptionService;
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
    private final OptionService optionService;
    private final MailService mailService;
    public AdminServiceImpl(OptionService optionService, UserService userService, MailService mailService,AbstractStringCacheStore cacheStore, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.cacheStore = cacheStore;
        this.optionService = optionService;
        this.mailService = mailService;
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

    @Override
    public void clearToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null){
            throw new BadRequestException("您尚未登陆, 因此无法注销");
        }

        User user = authentication.getDetail().getUser();

        cacheStore.getAny(SecurityUtils.buildAccessTokenKey(user), String.class).ifPresent(accessToken -> {
            cacheStore.delete(SecurityUtils.buildTokenAccessKey(accessToken));
            cacheStore.delete(SecurityUtils.buildAccessTokenKey(user));
        });

        cacheStore.getAny(SecurityUtils.buildRefreshTokenKey(user), String.class).ifPresent(refreshToken -> {
            cacheStore.delete(SecurityUtils.buildTokenRefreshKey(refreshToken));
            cacheStore.delete(SecurityUtils.buildRefreshTokenKey(user));
        });

        eventPublisher.publishEvent(new LogEvent(this, user.getUsername(), LogType.LOGGED_OUT, user.getNickname()));

        log.info("You have been logged out, looking forward to your next visit!");

    }

    @Override
    public void sendResetPasswordCode(ResetPasswordParam param) {
        cacheStore.getAny("code", String.class).ifPresent(code -> {
            throw new ServiceException("已经获取过验证码，不能重复获取");
        });

        if (!userService.verifyUser(param.getUsername(), param.getEmail())){
            throw new ServiceException("用户名或者邮箱验证错误");
        }

        String code = RandomUtil.randomNumbers(6);

        log.info("Got reset password code:{}", code);

        cacheStore.putAny("code", code, 5, TimeUnit.MINUTES);

        Boolean emailEnabled = optionService.getByPropertyOrDefault(EmailProperties.ENABLED, Boolean.class, false);

        if (!emailEnabled){
            throw new ServiceException("未启用 SMTP 服务，无法发送邮件，但是你可以通过系统日志找到验证码");
        }

        String content = "您正在进行密码重置操作，如不是本人操作，请尽快做好相应措施。密码重置验证码如下（五分钟有效）：\n" + code;
        mailService.sendTextMail(param.getEmail(), "找回密码验证码", content);

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
