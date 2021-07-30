package com.wjk.halo.repository.base;

import com.wjk.halo.model.entity.BaseMeta;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

@NoRepositoryBean
public interface BaseMetaRepository<META extends BaseMeta> extends BaseRepository<META, Long>, JpaSpecificationExecutor<META> {

    @NonNull
    List<META> deleteByPostId(@NonNull Integer postId);

    @NonNull
    List<META> findAllByPostIdIn(@NonNull Set<Integer> postIds);

    @NonNull
    List<META> findAllByPostId(@NonNull Integer postId);
}
