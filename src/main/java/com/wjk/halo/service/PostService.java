package com.wjk.halo.service;

import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.entity.PostMeta;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.params.PostQuery;
import com.wjk.halo.model.vo.ArchiveYearVO;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.model.vo.PostListVO;
import com.wjk.halo.service.base.BasePostService;
import javafx.geometry.Pos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PostService extends BasePostService<Post> {

    @NonNull
    PostDetailVO createBy(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, boolean autoSave);


    @NonNull
    PostDetailVO createBy(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, Set<PostMeta> metas, boolean autoSave);

    @NonNull
    Page<Post> pageBy(@NonNull PostQuery postQuery, @NonNull Pageable pageable);

    @NonNull
    Page<Post> pageBy(@NonNull String keyword, @NonNull Pageable pageable);

    @NonNull
    Page<PostListVO> convertToListVo(@NonNull Page<Post> postPage);

    @NonNull
    List<PostListVO> convertToListVo(@NonNull List<Post> posts);

    @NonNull
    PostDetailVO convertToDetailVo(@NonNull Post post);

    Page<PostDetailVO> convertToDetailVo(@NonNull Page<Post> postPage);

    @NonNull
    PostDetailVO updateBy(@NonNull Post postToUpdate, Set<Integer> tagIds, Set<Integer> categoryIds, Set<PostMeta> metas, boolean autoSave);

    @NonNull
    List<Post> removeByIds(@NonNull Collection<Integer> ids);

    @NonNull
    @Override
    Post getBy(@NonNull PostStatus status, @NonNull String slug);

    void publishVisitEvent(@NonNull Integer postId);

    @NotNull
    Sort getPostDefaultSort();

    List<ArchiveYearVO> convertToYearArchives(@NonNull List<Post> posts);

    @NonNull
    Post getBy(@NonNull Integer year, @NonNull String slug);

    @NonNull
    Post getBy(@NonNull Integer year, @NonNull Integer month, @NonNull String slug);

    @NonNull
    Post getBy(@NonNull Integer year, @NonNull Integer month, @NonNull Integer day, @NonNull String slug);

}
