package com.wjk.halo.service.impl;

import com.wjk.halo.exception.AlreadyExistsException;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.dto.post.BasePostSimpleDTO;
import com.wjk.halo.model.entity.BasePost;
import com.wjk.halo.model.enums.PostEditorType;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.properties.PostProperties;
import com.wjk.halo.repository.base.BasePostRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.service.base.BasePostService;
import com.wjk.halo.utils.DateUtils;
import com.wjk.halo.utils.HaloUtils;
import com.wjk.halo.utils.MarkdownUtils;
import com.wjk.halo.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
public abstract class BasePostServiceImpl<POST extends BasePost> extends AbstractCrudService<POST, Integer> implements BasePostService<POST> {

    private final Pattern summaryPattern = Pattern.compile("\t|\r|\n");

    private final BasePostRepository<POST> basePostRepository;

    private final OptionService optionService;

    public BasePostServiceImpl(BasePostRepository<POST> basePostRepository, OptionService optionService) {
        super(basePostRepository);
        this.basePostRepository = basePostRepository;
        this.optionService = optionService;
    }

    @Override
    public long countByStatus(PostStatus status) {
        return basePostRepository.countByStatus(status);
    }

    @Override
    @Transactional
    public POST createOrUpdateBy(POST post) {
        String originalContent = post.getOriginalContent();
        originalContent = HaloUtils.cleanHtmlTag(originalContent);

        post.setWordCount((long) originalContent.length());
        //根据编辑类型，设置默认内容
        if (post.getEditorType().equals(PostEditorType.MARKDOWN)){
            //编辑类型是markdown
            post.setFormatContent(MarkdownUtils.renderHtml(post.getOriginalContent()));
        }else {
            post.setFormatContent(post.getOriginalContent());
        }
        //如果设置了文章密码，或者文章类型为草稿，则文章状态为隐私
        if (StringUtils.isNotEmpty(post.getPassword()) && post.getStatus() != PostStatus.DRAFT){
            post.setStatus(PostStatus.INTIMATE);
        }
        //如果文章不存在，则创建
        if (ServiceUtils.isEmptyId(post.getId())){
            return create(post);
        }
        //否则，更新
        post.setEditTime(DateUtils.now());
        return update(post);

    }

    @Override
    public long countVisit() {
        return Optional.ofNullable(basePostRepository.countVisit()).orElse(0L);
    }

    @Override
    public long countLike() {
        return Optional.ofNullable(basePostRepository.countLike()).orElse(0L);
    }

    @Override
    public Page<BasePostSimpleDTO> convertToSimple(Page<POST> postPage) {
        return postPage.map(this::convertToSimple);
    }

    @Override
    public BasePostSimpleDTO convertToSimple(POST post) {
        BasePostSimpleDTO basePostSimpleDTO = new BasePostSimpleDTO().convertFrom(post);

        if (StringUtils.isBlank(basePostSimpleDTO.getSummary())){
            basePostSimpleDTO.setSummary(generateSummary(post.getFormatContent()));
        }
        return basePostSimpleDTO;
    }

    @Override
    public List<BasePostMinimalDTO> convertToMinimal(List<POST> posts) {
        if (CollectionUtils.isEmpty(posts)){
            return Collections.emptyList();
        }
        return posts.stream()
                .map(this::convertToMinimal)
                .collect(Collectors.toList());
    }

    @Override
    public BasePostMinimalDTO convertToMinimal(POST post) {
        return new BasePostMinimalDTO().convertFrom(post);
    }

    //page是页索引，top每页大小
    @Override
    public Page<POST> pageLatest(int top) {
        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "createTime"));
        return listAll(latestPageable);
    }

    @Override
    public Page<POST> pageBy(Pageable pageable) {
        return listAll(pageable);
    }


    @Override
    public Page<POST> pageBy(PostStatus status, Pageable pageable) {
        return basePostRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional
    public void increaseLike(Integer postId) {
        increaseLike(1L, postId);
    }

    @Override
    @Transactional
    public void increaseLike(long likes, Integer postId) {
        long affectedRows = basePostRepository.updateLikes(likes, postId);

        if (affectedRows != 1){
            log.error("Post with id: [{}] may not be found", postId);
            throw new BadRequestException("Failed to increase likes " + likes + " for post with id " + postId);
        }
    }

    @Override
    public POST updateStatus(PostStatus status, Integer postId) {
        POST post = getById(postId);

        if (!status.equals(post.getStatus())){
            int updatedRows = basePostRepository.updateStatus(status, postId);
            if (updatedRows != 1){
                throw new ServiceException("Failed to update post status of post with id " + postId);
            }
            post.setStatus(status);
        }
        //如果更新为published状态，则将原始的内容设置为当前的内容
        if (PostStatus.PUBLISHED.equals(status)){
            String formatContent = MarkdownUtils.renderHtml(post.getOriginalContent());
            int updatedRows = basePostRepository.updateFormatContent(formatContent, postId);

            if (updatedRows != 1){
                throw new ServiceException("Failed to update post format content of post with id " + postId);
            }

            post.setFormatContent(formatContent);
        }
        return post;

    }

    @Override
    public List<POST> updateStatusByIds(List<Integer> ids, PostStatus status) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return ids.stream().map(id -> {
            return updateStatus(status, id);
        }).collect(Collectors.toList());
    }

    @Override
    public POST updateDraftContent(String content, Integer postId) {
        if (content == null){
            content = "";
        }
        POST post = getById(postId);

        if (!StringUtils.equals(content, post.getOriginalContent())){
            int updateRows = basePostRepository.updateOriginalContent(content, postId);
            if (updateRows != 1){
                throw new ServiceException("Failed to update original content of post with id " + postId);
            }
            post.setOriginalContent(content);
        }
        return post;
    }

    @Override
    @Transactional
    public void increaseVisit(Integer postId) {
        increaseVisit(1L, postId);
    }

    @Override
    @Transactional
    public void increaseVisit(long visits, Integer postId) {
        boolean finishedIncrease;
        if (basePostRepository.getByIdAndStatus(postId, PostStatus.DRAFT).isPresent()){
            finishedIncrease = true;
            log.info("Post with id: [{}] is a draft and visits will not be updated", postId);
        }else {
            finishedIncrease = basePostRepository.updateVisit(visits, postId) == 1;
        }

        if (!finishedIncrease){
            log.error("Post with id: [{}] may not be found", postId);
            throw new BadRequestException("Failed to increase visits " + visits + " for post with id " + postId);
        }

    }

    @Override
    public POST getBy(PostStatus status, String slug) {
        Optional<POST> postOptional = basePostRepository.getBySlugAndStatus(slug, status);

        return postOptional.orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(slug));
    }

    @Override
    public Optional<POST> getPrevPost(POST post) {
        List<POST> posts = listPrevPosts(post, 1);

        return CollectionUtils.isEmpty(posts) ? Optional.empty() : Optional.of(posts.get(0));
    }

    @Override
    public List<POST> listPrevPosts(POST post, int size) {
        String indexSort = optionService.getByPropertyOfNonNull(PostProperties.INDEX_SORT).toString();

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, indexSort));

        switch (indexSort){
            case "createTime":
                return basePostRepository.findAllByStatusAndCreateTimeAfter(PostStatus.PUBLISHED, post.getCreateTime(), pageRequest).getContent();
            case "editTime":
                return basePostRepository.findAllByStatusAndEditTimeAfter(PostStatus.PUBLISHED, post.getEditTime(), pageRequest).getContent();
            case "visits":
                return basePostRepository.findAllByStatusAndVisitsAfter(PostStatus.PUBLISHED, post.getVisits(), pageRequest).getContent();
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public Optional<POST> getNextPost(POST post) {
        List<POST> posts = listNextPosts(post, 1);

        return CollectionUtils.isEmpty(posts) ? Optional.empty() : Optional.of(posts.get(0));
    }

    @Override
    public List<POST> listNextPosts(POST post, int size) {
        String indexSort = optionService.getByPropertyOfNonNull(PostProperties.INDEX_SORT).toString();

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(DESC, indexSort));

        switch (indexSort) {
            case "createTime":
                return basePostRepository.findAllByStatusAndCreateTimeBefore(PostStatus.PUBLISHED, post.getCreateTime(), pageRequest).getContent();
            case "editTime":
                return basePostRepository.findAllByStatusAndEditTimeBefore(PostStatus.PUBLISHED, post.getEditTime(), pageRequest).getContent();
            case "visits":
                return basePostRepository.findAllByStatusAndVisitsBefore(PostStatus.PUBLISHED, post.getVisits(), pageRequest).getContent();
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public String generateDescription(String content) {
        String text = HaloUtils.cleanHtmlTag(content);

        Matcher matcher = summaryPattern.matcher(text);
        text = matcher.replaceAll("");

        Integer summaryLength = optionService.getByPropertyOrDefault(PostProperties.SUMMARY_LENGTH, Integer.class, 150);

        return StringUtils.substring(text, 0, summaryLength);
    }


    @NonNull
    protected String generateSummary(@NonNull String htmlContent){
        String text = HaloUtils.cleanHtmlTag(htmlContent);

        Matcher matcher = summaryPattern.matcher(text);
        text = matcher.replaceAll("");

        Integer summaryLength = optionService.getByPropertyOrDefault(PostProperties.SUMMARY_LENGTH, Integer.class, 150);

        return StringUtils.substring(text, 0, summaryLength);
    }

    @Override
    public POST create(POST post) {
        slugMustNotExist(post);
        return super.create(post);
    }

    protected void slugMustNotExist(@NonNull POST post){
        boolean exist;

        if (ServiceUtils.isEmptyId(post.getId())){
            //创建
            exist = basePostRepository.existsBySlug(post.getSlug());
        }else {
            //更新别名
            exist = basePostRepository.existsByIdNotAndSlug(post.getId(), post.getSlug());
        }

        if (exist){
            throw new AlreadyExistsException("文章别名 " + post.getSlug() + " 已存在");
        }
    }

    @Override
    public POST update(POST post) {
        slugMustNotExist(post);
        return super.update(post);
    }
}
