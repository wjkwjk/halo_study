package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.PostTag;
import com.wjk.halo.repository.PostTagRepository;
import com.wjk.halo.service.PostTagService;
import com.wjk.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostTagServiceImpl extends AbstractCrudService<PostTag, Integer> implements PostTagService {

    private PostTagRepository postTagRepository;

    protected PostTagServiceImpl(PostTagRepository postTagRepository, PostTagRepository postTagRepository1) {
        super(postTagRepository);
        this.postTagRepository = postTagRepository1;
    }

    @Override
    public List<PostTag> removeByTagId(Integer tagId) {
        return postTagRepository.deleteByTagId(tagId);
    }

    @Override
    public List<PostTag> removeByPostId(Integer postId) {
        return postTagRepository.deletePostId(postId);
    }

    @Override
    public List<PostTag> mergeOrCreateByIfAbsent(Integer postId, Set<Integer> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)){
            return Collections.emptyList();
        }

        List<PostTag> postTagsStaging = tagIds.stream().map(tagId -> {
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tagId);
            return postTag;
        }).collect(Collectors.toList());

        List<PostTag> postTagsToRemove = new LinkedList<>();
        List<PostTag> postTagsToCreate = new LinkedList<>();

        List<PostTag> postTags = postTagRepository.findAllByPostId(postId);

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
}
