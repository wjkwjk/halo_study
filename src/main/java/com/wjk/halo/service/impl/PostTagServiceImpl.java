package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.PostTag;
import com.wjk.halo.model.entity.Tag;
import com.wjk.halo.repository.PostTagRepository;
import com.wjk.halo.repository.TagRepository;
import com.wjk.halo.service.PostTagService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.ServiceUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostTagServiceImpl extends AbstractCrudService<PostTag, Integer> implements PostTagService {

    private PostTagRepository postTagRepository;

    private final TagRepository tagRepository;

    protected PostTagServiceImpl(PostTagRepository postTagRepository,
                                 TagRepository tagRepository) {
        super(postTagRepository);
        this.postTagRepository = postTagRepository;
        this.tagRepository = tagRepository;
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
        //将文章与每一个tag关联
        List<PostTag> postTagsStaging = tagIds.stream().map(tagId -> {
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tagId);
            return postTag;
        }).collect(Collectors.toList());

        List<PostTag> postTagsToRemove = new LinkedList<>();
        List<PostTag> postTagsToCreate = new LinkedList<>();

        //找到与当前文章关联的tag
        List<PostTag> postTags = postTagRepository.findAllByPostId(postId);
        //如果某个post-tag不存在，则删除
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
}
