package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Option;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OptionRepository extends BaseRepository<Option, Integer>, JpaSpecificationExecutor<Option> {
}
