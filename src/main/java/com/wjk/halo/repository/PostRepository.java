package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Post;
import com.wjk.halo.repository.base.BasePostRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostRepository extends BasePostRepository<Post>, JpaSpecificationExecutor<Post> {
}
