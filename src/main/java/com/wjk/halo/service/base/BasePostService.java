package com.wjk.halo.service.base;

import com.wjk.halo.model.entity.BasePost;
import com.wjk.halo.model.enums.PostStatus;
import org.springframework.lang.NonNull;

public interface BasePostService<POST extends BasePost> extends CrudService<POST, Integer>{

    long countByStatus(PostStatus Status);

    @NonNull
    POST createOrUpdateBy(@NonNull POST post);

    long countVisit();

    long countLike();

}
