package com.wjk.halo.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.event.user.UserUpdateEvent;
import com.wjk.halo.exception.ForbiddenException;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.enums.MFAType;
import com.wjk.halo.model.params.UserParam;
import com.wjk.halo.repository.UserRepository;
import com.wjk.halo.service.UserService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.DateUtils;
import com.wjk.halo.utils.HaloUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends AbstractCrudService<User, Integer> implements UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        super(userRepository);
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getByUsernameOfNonNull(String username) {
        return getByUsername(username).orElseThrow(()->new NotFoundException("The username does not exist").setErrorData(username));
    }

    public Optional<User> getByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getCurrentUser() {
        List<User> users = listAll();

        if (CollectionUtils.isEmpty(users)){
            return Optional.empty();
        }

        return Optional.of(users.get(0));
    }

    @Override
    public void setPassword(@NonNull User user, @NonNull String plainPassword) {
        user.setPassword(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
        user.setMfaType(MFAType.NONE);
        user.setMfaKey(null);
    }

    @Override
    public User createBy(UserParam userParam) {
        User user = userParam.convertTo();
        setPassword(user, userParam.getPassword());
        return create(user);
    }

    @Override
    public boolean verifyUser(String username, String password) {

        User user = getCurrentUser().orElseThrow(() -> new ServiceException("未查询到博主信息"));
        return user.getUsername().equals(username) && user.getEmail().equals(password);
    }

    @Override
    public User updatePassword(String oldPassword, String newPasswod, Integer userId) {
        if (oldPassword.equals(newPasswod)){
            throw new BadRequestException("新密码和旧密码不能相同");
        }
        User user = getById(userId);

        if (!BCrypt.checkpw(oldPassword, user.getPassword())){
            throw new BadRequestException("旧密码错误").setErrorData(oldPassword);
        }

        setPassword(user, newPasswod);

        User updatedUser = update(user);

        eventPublisher.publishEvent(new LogEvent(this, updatedUser.getId().toString(), LogType.PASSWORD_UPDATED, HaloUtils.desensitize(oldPassword, 2, 1)));

        return updatedUser;

    }

    @Override
    @NonNull
    public User updateMFA(MFAType mfaType, String mfaKey, Integer userId) {
        User user = getById(userId);
        user.setMfaType(mfaType);
        user.setMfaKey((MFAType.NONE == mfaType) ? null : mfaKey);
        User updatedUser = update(user);
        eventPublisher.publishEvent(new LogEvent(this, updatedUser.getId().toString(), LogType.MFA_UPDATED, "MFA Type:" + mfaType));
        return updatedUser;
    }

    @Override
    public User getByEmailOfNonNull(String email) {
        return getByEmail(email).orElseThrow(()->new NotFoundException("The email does not exist").setErrorData(email));
    }

    @Override
    public void mustNotExpire(User user) {
        Date now = DateUtils.now();
        if (user.getExpireTime() != null && user.getExpireTime().after(now)){
            long seconds = TimeUnit.MILLISECONDS.toSeconds(user.getExpireTime().getTime() - now.getTime());
            throw new ForbiddenException("账号已被停用，请 " + HaloUtils.timeFormat(seconds) + " 后重试").setErrorData(seconds);
        }
    }

    @Override
    public boolean passwordMatch(User user, String plainPassword) {
        //数据库中的密码是先经过BCrypt.hashpw("password", BCrypt.gensalt())加密过的，如果使用没有加密过的会报错
        return !StringUtils.isBlank(plainPassword) && BCrypt.checkpw(plainPassword, user.getPassword());
    }


    @Override
    public User update(User user) {
        User updateUser = super.update(user);

        eventPublisher.publishEvent(new LogEvent(this, user.getId().toString(), LogType.PROFILE_UPDATED, user.getUsername()));
        eventPublisher.publishEvent(new UserUpdateEvent(this, user.getId()));
        return updateUser;
    }
}
