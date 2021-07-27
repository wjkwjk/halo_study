package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Post;
import com.wjk.halo.repository.base.BasePostRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends BasePostRepository<Post>, JpaSpecificationExecutor<Post> {
    @Override
    @Query("select sum(p.likes) from Post p")
    Long countVisit();

    @Override
    @Query("select sum(p.likes) from Post p")
    Long countLike();
}
