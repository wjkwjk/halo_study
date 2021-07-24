package com.wjk.halo.service.impl;

import cn.hutool.core.date.DateUtil;
import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.model.entity.*;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.enums.PostPermalinkType;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.repository.base.BasePostRepository;
import com.wjk.halo.service.*;
import com.wjk.halo.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;

@Slf4j
@Service
public class PostServiceImpl extends BasePostServiceImpl<Post> implements PostService {

    private final OptionService optionService;
    private final PostTagService postTagService;
    private final PostCategoryService postCategoryService;
    private final TagService tagService;
    private final CategoryService categoryService;
    private final PostMetaService postMetaService;
    private final ApplicationEventPublisher eventPublisher;
    private final PostCommentService postCommentService;

    public PostServiceImpl(BasePostRepository<Post> basePostRepository, OptionService optionService, PostTagService postTagService, PostCategoryService postCategoryService, TagService tagService, CategoryService categoryService, PostMetaService postMetaService, ApplicationEventPublisher eventPublisher, PostCommentService postCommentService) {
        super(basePostRepository, optionService);
        this.optionService = optionService;
        this.postTagService = postTagService;
        this.postCategoryService = postCategoryService;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.postMetaService = postMetaService;
        this.eventPublisher = eventPublisher;
        this.postCommentService = postCommentService;
    }

    //将post,tag,category整合，转变为PostDeatilVO
    @NonNull
    private PostDetailVO convertTo(@NonNull Post post, @Nullable List<Tag> tags, @Nullable List<Category> categories, List<PostMeta> postMetaList){
        //先复制post的属性到postdetailvo
        PostDetailVO postDetailVO = new PostDetailVO().convertFrom(post);

        if (StringUtils.isBlank(postDetailVO.getSummary())){
            postDetailVO.setSummary(generateSummary(post.getFormatContent()));
        }

        Set<Integer> tagIds = ServiceUtils.fetchProperty(tags, Tag::getId);
        Set<Integer> categoryIds = ServiceUtils.fetchProperty(categories, Category::getId);
        Set<Long> metaIds = ServiceUtils.fetchProperty(postMetaList, PostMeta::getId);

        postDetailVO.setTagIds(tagIds);
        postDetailVO.setTags(tagService.convertTo(tags));

        postDetailVO.setCategoryIds(categoryIds);
        postDetailVO.setCategories(categoryService.convertTo(categories));

        postDetailVO.setMetaIds(metaIds);
        postDetailVO.setMetas(postMetaService.convertTo(postMetaList));

        postDetailVO.setCommentCount(postCommentService.countByPostId(post.getId()));

        postDetailVO.setFullPath(buildFullPath(post));

        return postDetailVO;

    }

    private String buildFullPath(Post post){
        PostPermalinkType permalinkType = optionService.getPostPermalinkType();
        String pathSuffix = optionService.getPathSuffix();
        String archivePrefix = optionService.getArchivesPrefix();
        int month = DateUtil.month(post.getCreateTime()) + 1;
        String monthString = month < 10 ? "0" + month : String.valueOf(month);

        int day = DateUtil.dayOfMonth(post.getCreateTime());
        String dayString = day < 10 ? "0" + day:String.valueOf(day);

        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()){
            fullPath.append(optionService.getBlogBaseUrl());
        }
        fullPath.append(URL_SEPARATOR);

        if (permalinkType.equals(PostPermalinkType.DEFAULT)){
            fullPath.append(archivePrefix)
                    .append(URL_SEPARATOR)
                    .append(post.getSlug())
                    .append(pathSuffix);
        }else if (permalinkType.equals(PostPermalinkType.ID)){
            fullPath.append("?p=")
                    .append(post.getId());
        }else if (permalinkType.equals(PostPermalinkType.DATE)){
            fullPath.append(DateUtil.year(post.getCreateTime()))
                    .append(URL_SEPARATOR)
                    .append(monthString)
                    .append(URL_SEPARATOR)
                    .append(post.getSlug())
                    .append(pathSuffix);
        }else if (permalinkType.equals(PostPermalinkType.DAY)) {
            fullPath.append(DateUtil.year(post.getCreateTime()))
                    .append(URL_SEPARATOR)
                    .append(monthString)
                    .append(URL_SEPARATOR)
                    .append(dayString)
                    .append(URL_SEPARATOR)
                    .append(post.getSlug())
                    .append(pathSuffix);
        } else if (permalinkType.equals(PostPermalinkType.YEAR)) {
            fullPath.append(DateUtil.year(post.getCreateTime()))
                    .append(URL_SEPARATOR)
                    .append(post.getSlug())
                    .append(pathSuffix);
        }
        return fullPath.toString();

    }

    @Override
    public PostDetailVO createBy(Post postToCreate, Set<Integer> tagIds, Set<Integer> categoryIds, boolean autoSave) {
        PostDetailVO createdPost = createOrUpdate(postToCreate, tagIds, categoryIds, null);
        if (!autoSave){
            LogEvent logEvent = new LogEvent(this, createdPost.getId().toString(), LogType.POST_PUBLISHED, createdPost.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdPost;
    }

    @Override
    @Transactional
    public PostDetailVO createBy(Post postToCreate, Set<Integer> tagIds, Set<Integer> categoryIds, Set<PostMeta> metas, boolean autoSave) {
        PostDetailVO createdPost = createOrUpdate(postToCreate, tagIds, categoryIds, metas);
        if (!autoSave){
            LogEvent logEvent = new LogEvent(this, createdPost.getId().toString(), LogType.POST_PUBLISHED, createdPost.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdPost;
    }


    private PostDetailVO createOrUpdate(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, Set<PostMeta> metas){
        //创建或者更新文章
        post = super.createOrUpdateBy(post);

        postTagService.removeByPostId(post.getId());

        postCategoryService.removeByPostId(post.getId());

        //列出所有tag
        List<Tag> tags = tagService.listAllByIds(tagIds);

        //列出所有category
        List<Category> categories = categoryService.listAllByIds(categoryIds);

        List<PostTag> postTags = postTagService.mergeOrCreateByIfAbsent(post.getId(), ServiceUtils.fetchProperty(tags, Tag::getId));
        log.debug("Created post tags: [{}]", postTags);

        List<PostCategory> postCategories = postCategoryService.mergeOrCreateByIfAbsent(post.getId(), ServiceUtils.fetchProperty(categories, Category::getId));
        log.debug("Created post categories: [{}]", postCategories);

        List<PostMeta> postMetaList = postMetaService.createOrUpdateByPostId(post.getId(), metas);
        log.debug("Created post metas: [{}]", postMetaList);

        //将信息转变为post detail vo
        return convertTo(post, tags, categories, postMetaList);

    }

}
