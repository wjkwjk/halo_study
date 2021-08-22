package com.wjk.halo.service.impl;

import cn.hutool.core.date.DateUtil;
import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.event.post.PostVisitEvent;
import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.dto.post.BasePostSimpleDTO;
import com.wjk.halo.model.entity.*;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.enums.PostPermalinkType;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.params.PostQuery;
import com.wjk.halo.model.properties.PostProperties;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.model.vo.PostListVO;
import com.wjk.halo.repository.PostRepository;
import com.wjk.halo.repository.base.BasePostRepository;
import com.wjk.halo.service.*;
import com.wjk.halo.utils.DateUtils;
import com.wjk.halo.utils.ServiceUtils;
import javafx.geometry.Pos;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

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
    private final PostRepository postRepository;

    public PostServiceImpl(BasePostRepository<Post> basePostRepository,
                           OptionService optionService,
                           PostRepository postRepository,
                           TagService tagService,
                           CategoryService categoryService,
                           PostTagService postTagService,
                           PostCategoryService postCategoryService,
                           PostCommentService postCommentService,
                           ApplicationEventPublisher eventPublisher,
                           PostMetaService postMetaService) {
        super(basePostRepository, optionService);
        this.postRepository = postRepository;
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

    @Override
    public Page<Post> pageBy(PostQuery postQuery, Pageable pageable) {
        return postRepository.findAll(buildSpecByQuery(postQuery), pageable);
    }

    @Override
    public Page<Post> pageBy(String keyword, Pageable pageable) {
        PostQuery postQuery = new PostQuery();
        postQuery.setKeyword(keyword);
        postQuery.setStatus(PostStatus.PUBLISHED);

        return postRepository.findAll(buildSpecByQuery(postQuery), pageable);
    }

    @Override
    public Page<PostListVO> convertToListVo(Page<Post> postPage) {
        List<Post> posts = postPage.getContent();
        //获取id
        Set<Integer> postIds = ServiceUtils.fetchProperty(posts, Post::getId);

        // Get tag list map
        Map<Integer, List<Tag>> tagListMap = postTagService.listTagListMapBy(postIds);

        // Get category list map
        Map<Integer, List<Category>> categoryListMap = postCategoryService.listCategoryListMap(postIds);

        // Get comment count
        Map<Integer, Long> commentCountMap = postCommentService.countByPostIds(postIds);

        // Get post meta list map
        Map<Integer, List<PostMeta>> postMetaListMap = postMetaService.listPostMetaAsMap(postIds);

        return postPage.map(post -> {
            PostListVO postListVO = new PostListVO().convertFrom(post);

            if (StringUtils.isBlank(postListVO.getSummary())){
                postListVO.setSummary(generateSummary(post.getFormatContent()));
            }

            Optional.ofNullable(tagListMap.get(post.getId())).orElseGet(LinkedList::new);

            postListVO.setTags(Optional.ofNullable(tagListMap.get(post.getId()))
                .orElseGet(LinkedList::new)
                .stream()
                .filter(Objects::nonNull)
                .map(tagService::convertTo)
                .collect(Collectors.toList()));

            postListVO.setCategories(Optional.ofNullable(categoryListMap.get(post.getId()))
                .orElseGet(LinkedList::new)
                .stream()
                .filter(Objects::nonNull)
                .map(categoryService::convertTo)
                .collect(Collectors.toList()));

            List<PostMeta> metas = Optional.ofNullable(postMetaListMap.get(post.getId()))
                    .orElseGet(LinkedList::new);
            postListVO.setMetas(postMetaService.convertToMap(metas));

            postListVO.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0L));

            postListVO.setFullPath(buildFullPath(post));

            return postListVO;
        });

    }

    @Override
    public List<PostListVO> convertToListVo(List<Post> posts) {

        Set<Integer> postIds = ServiceUtils.fetchProperty(posts, Post::getId);

        Map<Integer, List<Tag>> tagListMap = postTagService.listTagListMapBy(postIds);

        Map<Integer, List<Category>> categoryListMap = postCategoryService.listCategoryListMap(postIds);

        Map<Integer, Long> commentCountMap = postCommentService.countByPostIds(postIds);

        Map<Integer, List<PostMeta>> postMetaListMap = postMetaService.listPostMetaAsMap(postIds);

        return posts.stream().map(post -> {
            PostListVO postListVO = new PostListVO().convertFrom(post);

            if (StringUtils.isBlank(postListVO.getSummary())){
                postListVO.setSummary(generateSummary(post.getFormatContent()));
            }

            Optional.ofNullable(tagListMap.get(post.getId())).orElseGet(LinkedList::new);

            postListVO.setTags(Optional.ofNullable(tagListMap.get(post.getId()))
                .orElseGet(LinkedList::new)
                .stream()
                .filter(Objects::nonNull)
                .map(tagService::convertTo)
                .collect(Collectors.toList()));

            postListVO.setCategories(Optional.ofNullable(categoryListMap.get(post.getId()))
                    .orElseGet(LinkedList::new)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(categoryService::convertTo)
                    .collect(Collectors.toList()));

            // Set post metas
            List<PostMeta> metas = Optional.ofNullable(postMetaListMap.get(post.getId()))
                    .orElseGet(LinkedList::new);
            postListVO.setMetas(postMetaService.convertToMap(metas));

            // Set comment count
            postListVO.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0L));

            postListVO.setFullPath(buildFullPath(post));

            return postListVO;

        }).collect(Collectors.toList());
    }

    @Override
    public PostDetailVO convertToDetailVo(Post post) {
        List<Tag> tags = postTagService.listTagsBy(post.getId());

        List<Category> categories = postCategoryService.listCategoriesBy(post.getId());

        List<PostMeta> metas = postMetaService.listBy(post.getId());

        return convertTo(post, tags, categories, metas);
    }

    @Override
    @Transactional
    public PostDetailVO updateBy(Post postToUpdate, Set<Integer> tagIds, Set<Integer> categoryIds, Set<PostMeta> metas, boolean autoSave) {
        postToUpdate.setEditTime(DateUtils.now());
        PostDetailVO updatedPost = createOrUpdate(postToUpdate, tagIds, categoryIds, metas);
        if (!autoSave){
            LogEvent logEvent = new LogEvent(this, updatedPost.getId().toString(),
                    LogType.POST_EDITED, updatedPost.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return updatedPost;
    }

    @Override
    public Post removeById(Integer postId) {
        log.debug("Removing post: [{}]", postId);

        // Remove post tags
        List<PostTag> postTags = postTagService.removeByPostId(postId);

        log.debug("Removed post tags: [{}]", postTags);

        // Remove post categories
        List<PostCategory> postCategories = postCategoryService.removeByPostId(postId);

        log.debug("Removed post categories: [{}]", postCategories);

        // Remove metas
        List<PostMeta> metas = postMetaService.removeByPostId(postId);
        log.debug("Removed post metas: [{}]", metas);

        // Remove post comments
        List<PostComment> postComments = postCommentService.removeByPostId(postId);
        log.debug("Removed post comments: [{}]", postComments);

        Post deletedPost = super.removeById(postId);

        // Log it
        eventPublisher.publishEvent(new LogEvent(this, postId.toString(), LogType.POST_DELETED,
                deletedPost.getTitle()));

        return deletedPost;
    }

    @Override
    public List<Post> removeByIds(Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return ids.stream().map(this::removeById).collect(Collectors.toList());
    }

    @NonNull
    private Specification<Post> buildSpecByQuery(@NonNull PostQuery postQuery){
        return (Specification<Post>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();
            if (postQuery.getStatus() != null){
                predicates.add(criteriaBuilder.equal(root.get("status"), postQuery.getStatus()));
            }

            if (postQuery.getCategoryId() != null){
                Subquery<Post> postSubquery = query.subquery(Post.class);
                Root<PostCategory> postCategoryRoot = postSubquery.from(PostCategory.class);
                postSubquery.select(postCategoryRoot.get("postId"));
                postSubquery.where(
                        criteriaBuilder.equal(root.get("id"), postCategoryRoot.get("postId")),
                        criteriaBuilder.equal(postCategoryRoot.get("categoryId"), postQuery.getCategoryId()));
                predicates.add(criteriaBuilder.exists(postSubquery));
            }

            if (postQuery.getKeyword() != null){
                String likeCondition = String.format("%%%s%%", StringUtils.strip(postQuery.getKeyword()));

                Predicate titleLike = criteriaBuilder.like(root.get("title"), likeCondition);
                Predicate originalContentLike = criteriaBuilder.like(root.get("originalContent"), likeCondition);

                predicates.add(criteriaBuilder.or(titleLike, originalContentLike));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
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

    @Override
    public BasePostSimpleDTO convertToSimple(Post post) {
        BasePostSimpleDTO basePostSimpleDTO = new BasePostSimpleDTO().convertFrom(post);

        if (StringUtils.isBlank(basePostSimpleDTO.getSummary())){
            basePostSimpleDTO.setSummary(generateSummary(post.getFormatContent()));
        }
        basePostSimpleDTO.setFullPath(buildFullPath(post));
        return basePostSimpleDTO;
    }

    @Override
    public BasePostMinimalDTO convertToMinimal(Post post) {
        BasePostMinimalDTO basePostMinimalDTO = new BasePostMinimalDTO().convertFrom(post);

        basePostMinimalDTO.setFullPath(buildFullPath(post));

        return basePostMinimalDTO;
    }

    @Override
    public Post getBy(PostStatus status, String slug) {
        return super.getBy(status, slug);
    }

    @Override
    public void publishVisitEvent(Integer postId) {
        eventPublisher.publishEvent(new PostVisitEvent(this, postId));
    }

    @Override
    public @NotNull Sort getPostDefaultSort() {
        String indexSort = optionService.getByPropertyOfNonNull(PostProperties.INDEX_SORT)
                .toString();
        return Sort.by(DESC, "topPriority").and(Sort.by(DESC, indexSort).and(Sort.by(DESC, "id")));
    }

    @Override
    public List<BasePostMinimalDTO> convertToMinimal(List<Post> posts) {
        if (CollectionUtils.isEmpty(posts)){
            return Collections.emptyList();
        }
        return posts.stream()
                .map(this::convertToMinimal)
                .collect(Collectors.toList());

    }
}
