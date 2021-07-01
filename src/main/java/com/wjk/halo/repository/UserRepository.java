package com.wjk.halo.repository;

import com.wjk.halo.model.entity.User;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Integer> {

    @NonNull
    Optional<User> findByUsername(@NonNull String username);

    @NonNull
    Optional<User> findByEmail(@NonNull String email);

}
