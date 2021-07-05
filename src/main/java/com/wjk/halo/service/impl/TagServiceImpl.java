package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.Tag;
import com.wjk.halo.repository.TagRepository;
import com.wjk.halo.service.TagService;
import com.wjk.halo.service.base.AbstractCrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TagServiceImpl extends AbstractCrudService<Tag, Integer> implements TagService {

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        super(tagRepository);
        this.tagRepository = tagRepository;
    }
}
