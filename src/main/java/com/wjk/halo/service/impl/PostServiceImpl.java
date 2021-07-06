package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.*;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.repository.base.BasePostRepository;
import com.wjk.halo.service.*;
import com.wjk.halo.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class PostServiceImpl extends BasePostServiceImpl<Post> implements PostService {

    private final OptionService optionService;
    private final PostTagService postTagService;
    private final PostCategoryService postCategoryService;
    private final TagService tagService;
    private final CategoryService categoryService;
    private final PostMetaService postMetaService;

    public PostServiceImpl(BasePostRepository<Post> basePostRepository, OptionService optionService, PostTagService postTagService, PostCategoryService postCategoryService, TagService tagService, CategoryService categoryService, PostMetaService postMetaService) {
        super(basePostRepository, optionService);
        this.optionService = optionService;
        this.postTagService = postTagService;
        this.postCategoryService = postCategoryService;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.postMetaService = postMetaService;
    }


    @NonNull
    private PostDetailVO convertTo(@NonNull Post post, @Nullable List<Tag> tags, @Nullable List<Category> categories, List<PostMeta> postMetaList){
        PostDetailVO postDetailVO = new PostDetailVO().convertFrom(post);

        if (StringUtils.isBlank(postDetailVO.getSummary())){
            postDetailVO.setSummary(generateSummary(post.getFormatContent()));
        }

        Set<Integer> tagIds = ServiceUtils.fetchProperty(tags, Tag::getId);
        Set<Integer> categoryIds = ServiceUtils.fetchProperty()

    }

    @Override
    public PostDetailVO createBy(Post post, Set<Integer> tagIds, Set<Integer> categoryIds, boolean autoSave) {
        PostDetailVO createdPost = crea
    }


    private PostDetailVO createOrUpdate(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, Set<PostMeta> metas){
        post = super.createOrUpdateBy(post);

        postTagService.removeByTagId(post.getId());

        postTagService.removeByPostId(post.getId());

        postCategoryService.removeByPostId(post.getId());

        List<Tag> tags = tagService.listAllByIds(tagIds);

        List<Category> categories = categoryService.listAllByIds(categoryIds);

        List<PostTag> postTags = postTagService.mergeOrCreateByIfAbsent(post.getId(), ServiceUtils.fetchProperty(tags, Tag::getId));

        log.debug("Created post tags: [{}]", postTags);

        List<PostCategory> postCategories = postCategoryService.mergeOrCreateByIfAbsent(post.getId(), ServiceUtils.fetchProperty(categories, Category::getId));

        log.debug("Created post categories: [{}]", postCategories);

        List<PostMeta> postMetaList = postMetaService.createOrUpdateByPostId(post.getId(), metas);

        log.debug("Created post metas: [{}]", postMetaList);

        return con




    }

}
