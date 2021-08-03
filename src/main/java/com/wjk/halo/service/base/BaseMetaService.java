package com.wjk.halo.service.base;

import com.wjk.halo.model.dto.BaseMetaDTO;
import com.wjk.halo.model.entity.BaseMeta;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BaseMetaService<META extends BaseMeta> extends CrudService<META, Long> {

    List<META> createOrUpdateByPostId(@NonNull Integer postId, Set<META> metas);

    List<META> removeByPostId(@NonNull Integer postId);

    @NonNull
    BaseMetaDTO convertTo(@NonNull META postmeta);

    @NonNull
    List<BaseMetaDTO> convertTo(@NonNull List<META> postMetaList);

    Map<Integer, List<META>> listPostMetaAsMap(@NonNull Set<Integer> postIds);

    Map<String, Object> convertToMap(List<META> metas);

    @NonNull
    List<META> listBy(@NonNull Integer postId);

    @NonNull
    @Override
    META create(@NonNull META meta);

    void validateTarget(@NonNull Integer targetId);
}
