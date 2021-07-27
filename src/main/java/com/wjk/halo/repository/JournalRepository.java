package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Journal;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JournalRepository extends BaseRepository<Journal, Integer>, JpaSpecificationExecutor<Journal> {
}
