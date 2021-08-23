package com.wjk.halo.service;

import com.wjk.halo.model.dto.TagDTO;
import com.wjk.halo.model.entity.Tag;
import com.wjk.halo.service.base.CrudService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface TagService extends CrudService<Tag, Integer> {

    @NonNull
    TagDTO convertTo(@NonNull Tag tag);

    @NonNull
    List<TagDTO> convertTo(@Nullable List<Tag> tags);

    @NonNull
    Tag getBySlugOfNonNull(@NonNull String slug);
}
