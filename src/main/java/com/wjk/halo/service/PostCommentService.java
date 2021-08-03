package com.wjk.halo.service;

import com.wjk.halo.model.entity.PostComment;
import com.wjk.halo.model.vo.PostCommentWithPostVO;
import com.wjk.halo.service.base.BaseCommentService;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface PostCommentService extends BaseCommentService<PostComment> {

    @NonNull
    Page<PostCommentWithPostVO> convertToWithPostVo(@NonNull Page<PostComment> commentPage);

    @NonNull
    List<PostCommentWithPostVO> convertToWithPostVo(@Nullable List<PostComment> postComments);

}
