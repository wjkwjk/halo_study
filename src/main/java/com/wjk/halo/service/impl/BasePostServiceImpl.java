package com.wjk.halo.service.impl;

import com.wjk.halo.exception.AlreadyExistsException;
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
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            exist = basePostRepository.existByIdNotAndSlug(post.getId(), post.getSlug());
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
