package com.wjk.halo.service;

import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.MFAType;
import com.wjk.halo.model.params.UserParam;
import com.wjk.halo.service.base.CrudService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Null;
import java.util.Optional;

public interface UserService extends CrudService<User, Integer> {

    @NonNull
    User getByUsernameOfNonNull(@NonNull String username);

    @NonNull
    User getByEmailOfNonNull(@NonNull String email);

    void mustNotExpire(@NonNull User user);

    boolean passwordMatch(@NonNull User user, @Nullable String plainPassword);

    @NonNull
    Optional<User> getByUsername(@NonNull String username);

    @NonNull
    Optional<User> getByEmail(@NonNull String email);

    @NonNull
    Optional<User> getCurrentUser();

    void setPassword(@NonNull User user, @NonNull String plainPassword);

    @NonNull
    User createBy(@NonNull UserParam userParam);

    boolean verifyUser(@NonNull String username, @NonNull String password);

    @NonNull
    User updatePassword(@NonNull String oldPassword, @NonNull String newPasswod, @NonNull Integer userId);

    @NonNull
    User updateMFA(@NonNull MFAType mfaType, String mfaKey, @NonNull Integer userId);
}
