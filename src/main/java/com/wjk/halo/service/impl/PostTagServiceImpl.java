package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.TagWithPostCountDTO;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.entity.PostTag;
import com.wjk.halo.model.entity.Tag;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.projection.TagPostPostCountProjection;
import com.wjk.halo.repository.PostRepository;
import com.wjk.halo.repository.PostTagRepository;
import com.wjk.halo.repository.TagRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostTagService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.ServiceUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;

@Service
public class PostTagServiceImpl extends AbstractCrudService<PostTag, Integer> implements PostTagService {

    private PostTagRepository postTagRepository;

    private PostRepository postRepository;

    private final TagRepository tagRepository;

    private final OptionService optionService;

    protected PostTagServiceImpl(PostTagRepository postTagRepository,
                                 PostRepository postRepository,
                                 TagRepository tagRepository,
                                 OptionService optionService) {
        super(postTagRepository);
        this.postTagRepository = postTagRepository;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.optionService = optionService;
    }

    @Override
    public List<PostTag> removeByTagId(Integer tagId) {
        return postTagRepository.deleteByTagId(tagId);
    }

    @Override
    public List<PostTag> removeByPostId(Integer postId) {
        return postTagRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostTag> mergeOrCreateByIfAbsent(Integer postId, Set<Integer> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)){
            return Collections.emptyList();
        }
        //?????????????????????tag??????
        List<PostTag> postTagsStaging = tagIds.stream().map(tagId -> {
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tagId);
            return postTag;
        }).collect(Collectors.toList());

        List<PostTag> postTagsToRemove = new LinkedList<>();
        List<PostTag> postTagsToCreate = new LinkedList<>();

        //??????????????????????????????tag
        List<PostTag> postTags = postTagRepository.findAllByPostId(postId);
        //????????????post-tag?????????????????????
        postTags.forEach(postTag -> {
            if (!postTagsStaging.contains(postTag)){
                postTagsToRemove.add(postTag);
            }
        });

        postTagsStaging.forEach(postTagStaging -> {
            if (!postTags.contains(postTagStaging)){
                postTagsToCreate.add(postTagStaging);
            }
        });

        removeAll(postTagsToRemove);

        postTags.addAll(createInBatch(postTagsToCreate));

        return postTags;

    }

    @Override
    public Map<Integer, List<Tag>> listTagListMapBy(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)){
            return Collections.emptyMap();
        }

        List<PostTag> postTags = postTagRepository.findAllByPostIdIn(postIds);

        Set<Integer> tagIds = ServiceUtils.fetchProperty(postTags, PostTag::getTagId);

        List<Tag> tags = tagRepository.findAllById(tagIds);

        Map<Integer, Tag> tagMap = ServiceUtils.convertToMap(tags, Tag::getId);

        Map<Integer, List<Tag>> tagListMap = new HashMap<>();

        postTags.forEach(postTag -> tagListMap.computeIfAbsent(postTag.getPostId(), postId -> new LinkedList<>()).add(tagMap.get(postTag.getTagId())));

        return tagListMap;
    }

    @Override
    public List<Tag> listTagsBy(Integer postId) {
        Set<Integer> tagIds = postTagRepository.findAllTagIdsByPostId(postId);
        return tagRepository.findAllById(tagIds);
    }

    @Override
    public List<TagWithPostCountDTO> listTagWithCountDtos(Sort sort) {
        List<Tag> tags = tagRepository.findAll(sort);

        Map<Integer, Long> tagPostCountMap = ServiceUtils.convertToMap(postTagRepository.findPostCount(), TagPostPostCountProjection::getTagId, TagPostPostCountProjection::getPostCount);

        return tags.stream().map(
                tag -> {
                    TagWithPostCountDTO tagWithCountOutputDTO = new TagWithPostCountDTO().convertFrom(tag);
                    tagWithCountOutputDTO.setPostCount(tagPostCountMap.getOrDefault(tag.getId(), 0L));

                    StringBuilder fullPath = new StringBuilder();

                    if (optionService.isEnabledAbsolutePath()){
                        fullPath.append(optionService.getBlogBaseUrl());
                    }

                    fullPath.append(URL_SEPARATOR)
                            .append(optionService.getTagsPrefix())
                            .append(URL_SEPARATOR)
                            .append(tag.getSlug())
                            .append(optionService.getPathSuffix());

                    tagWithCountOutputDTO.setFullPath(fullPath.toString());

                    return tagWithCountOutputDTO;

                }
        ).collect(Collectors.toList());
    }

    @Override
    public Page<Post> pagePostsBy(Integer tagId, PostStatus status, Pageable pageable) {
        Set<Integer> postIds = postTagRepository.findAllPostIdsByTagId(tagId, status);

        return postRepository.findAllByIdIn(postIds, pageable);
    }
}
