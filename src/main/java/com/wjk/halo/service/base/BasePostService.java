package com.wjk.halo.service.base;

import com.wjk.halo.model.entity.BasePost;
import com.wjk.halo.model.enums.PostStatus;

public interface BasePostService<POST extends BasePost> extends CrudService<POST, Integer>{

    long countByStatus(PostStatus status);

}
