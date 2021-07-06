package com.wjk.halo.service.base;

import com.wjk.halo.model.entity.BaseMeta;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

public interface BaseMetaService<META extends BaseMeta> extends CrudService<META, Long> {

    List<META> createOrUpdateByPostId(@NonNull Integer postId, Set<META> metas);

    List<META> removeByPostId(@NonNull Integer postId);
}
