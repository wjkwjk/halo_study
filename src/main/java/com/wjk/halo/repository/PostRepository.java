package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Post;
import com.wjk.halo.repository.base.BasePostRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends BasePostRepository<Post>, JpaSpecificationExecutor<Post> {
    @Override
    @Query("select sum(p.likes) from Post p")
    Long countVisit();

    @Override
    @Query("select sum(p.likes) from Post p")
    Long countLike();

    @Query("select post from Post post where year(post.createTime) = :year and post.slug = :slug")
    Optional<Post> findBy(@Param("year") Integer year, @Param("slug") String slug);

    @Query("select post from Post post where year(post.createTime) = :year and month(post.createTime) = :month and post.slug = :slug")
    Optional<Post> findBy(@Param("year") Integer year, @Param("month") Integer month, @Param("slug") String slug);

    @Query("select post from Post post where year(post.createTime) = :year and month(post.createTime) = :month and day(post.createTime) = :day and post.slug = :slug")
    Optional<Post> findBy(@Param("year") Integer year, @Param("month") Integer month, @Param("day") Integer day, @Param("slug") String slug);

}
