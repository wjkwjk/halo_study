package com.wjk.halo.repository.base;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseRepository<DOMAIN, ID> extends JpaRepository<DOMAIN, ID> {
}
