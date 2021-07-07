package com.wjk.halo.repository.base;

import com.wjk.halo.model.entity.BaseComment;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

@NoRepositoryBean
public interface BaseCommentRepository<COMMENT extends BaseComment> extends BaseRepository<COMMENT, Long>, JpaSpecificationExecutor<COMMENT> {

    long countByPostId(@NonNull Integer postId);

}
