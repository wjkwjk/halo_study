package com.wjk.halo.service.impl;

import com.wjk.halo.exception.AlreadyExistsException;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.dto.TagDTO;
import com.wjk.halo.model.entity.Tag;
import com.wjk.halo.repository.TagRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.TagService;
import com.wjk.halo.service.base.AbstractCrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;

@Slf4j
@Service
public class TagServiceImpl extends AbstractCrudService<Tag, Integer> implements TagService {

    private final TagRepository tagRepository;
    private final OptionService optionService;

    public TagServiceImpl(TagRepository tagRepository, OptionService optionService) {
        super(tagRepository);
        this.tagRepository = tagRepository;
        this.optionService = optionService;
    }

    @Override
    public TagDTO convertTo(Tag tag) {
        TagDTO tagDTO = new TagDTO().convertFrom(tag);
        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()){
            fullPath.append(optionService.getBlogBaseUrl());
        }

        fullPath.append(URL_SEPARATOR)
                .append(optionService.getTagsPrefix())
                .append(URL_SEPARATOR)
                .append(tag.getSlug())
                .append(optionService.getPathSuffix());
        tagDTO.setFullPath(fullPath.toString());

        return tagDTO;
    }

    @Override
    public List<TagDTO> convertTo(List<Tag> tags) {
        if (CollectionUtils.isEmpty(tags)){
            return Collections.emptyList();
        }
        return tags.stream()
                .map(this::convertTo)
                .collect(Collectors.toList());
    }

    @Override
    public Tag getBySlugOfNonNull(String slug) {
        return tagRepository.getBySlug(slug).orElseThrow(() -> new NotFoundException("查询不到该标签的信息").setErrorData(slug));

    }

    @Override
    @Transactional
    public Tag create(Tag tag) {
        // Check if the tag is exist
        long count = tagRepository.countByNameOrSlug(tag.getName(), tag.getSlug());

        log.debug("Tag count: [{}]", count);

        if (count > 0) {
            // If the tag has exist already
            throw new AlreadyExistsException("该标签已存在").setErrorData(tag);
        }

        // Get tag name
        return super.create(tag);
    }
}
