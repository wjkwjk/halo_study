package com.wjk.halo.controller.content.model;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.exception.ForbiddenException;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.entity.PostMeta;
import com.wjk.halo.model.entity.Tag;
import com.wjk.halo.model.enums.PostEditorType;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.support.HaloConst;
import com.wjk.halo.model.vo.PostListVO;
import com.wjk.halo.service.*;
import com.wjk.halo.utils.MarkdownUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostModel {

    private final PostService postService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final CategoryService categoryService;

    private final PostTagService postTagService;

    private final TagService tagService;

    private final PostMetaService postMetaService;

    private final OptionService optionService;

    private final AbstractStringCacheStore cacheStore;

    public PostModel(PostService postService,
                     ThemeService themeService,
                     PostCategoryService postCategoryService,
                     CategoryService categoryService,
                     PostMetaService postMetaService,
                     PostTagService postTagService,
                     TagService tagService,
                     OptionService optionService,
                     AbstractStringCacheStore cacheStore) {
        this.postService = postService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.categoryService = categoryService;
        this.postTagService = postTagService;
        this.tagService = tagService;
        this.postMetaService = postMetaService;
        this.optionService = optionService;
        this.cacheStore = cacheStore;
    }

    public String content(Post post, String token, Model model){
        if (post.getStatus().equals(PostStatus.INTIMATE) && StringUtils.isEmpty(token)){
            model.addAttribute("slug", post.getSlug());
            return "common/template/post_password";
        }

        if (StringUtils.isEmpty(token)){
            post = postService.getBy(PostStatus.PUBLISHED, post.getSlug());
        }else {
            String cachedToken = cacheStore.getAny(token, String.class).orElseThrow(() -> new ForbiddenException("您没有该文章的访问权限"));
            if (!cachedToken.equals(token)){
                throw new ForbiddenException("您没有该文章的访问权限");
            }
            if (post.getEditorType().equals(PostEditorType.MARKDOWN)){
                post.setFormatContent(MarkdownUtils.renderHtml(post.getOriginalContent()));
            }else {
                post.setFormatContent(post.getOriginalContent());
            }
        }

        postService.publishVisitEvent(post.getId());

        postService.getPrevPost(post).ifPresent(prevPost -> model.addAttribute("prevPost", postService.convertToDetailVo(prevPost)));
        postService.getNextPost(post).ifPresent(nextPost -> model.addAttribute("nextPost", postService.convertToDetailVo(nextPost)));

        List<Category> categories = postCategoryService.listCategoriesBy(post.getId());
        List<Tag> tags = postTagService.listTagsBy(post.getId());
        List<PostMeta> metas = postMetaService.listBy(post.getId());

        // Generate meta keywords.
        if (StringUtils.isNotEmpty(post.getMetaKeywords())) {
            model.addAttribute("meta_keywords", post.getMetaKeywords());
        } else {
            model.addAttribute("meta_keywords", tags.stream().map(Tag::getName).collect(Collectors.joining(",")));
        }

        // Generate meta description.
        if (StringUtils.isNotEmpty(post.getMetaDescription())) {
            model.addAttribute("meta_description", post.getMetaDescription());
        } else {
            model.addAttribute("meta_description", postService.generateDescription(post.getFormatContent()));
        }

        model.addAttribute("is_post", true);
        model.addAttribute("post", postService.convertToDetailVo(post));
        model.addAttribute("categories", categoryService.convertTo(categories));
        model.addAttribute("tags", tagService.convertTo(tags));
        model.addAttribute("metas", postMetaService.convertToMap(metas));

        if (themeService.templateExists(
                ThemeService.CUSTOM_POST_PREFIX + post.getTemplate() + HaloConst.SUFFIX_FTL)){
            return themeService.render(ThemeService.CUSTOM_POST_PREFIX + post.getTemplate());
        }

        return themeService.render("post");

    }

    public String list(Integer page, Model model) {
        int pageSize = optionService.getPostPageSize();
        Pageable pageable = PageRequest
                .of(page >= 1 ? page - 1 : page, pageSize, postService.getPostDefaultSort());

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postService.convertToListVo(postPage);

        model.addAttribute("is_index", true);
        model.addAttribute("posts", posts);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("index");
    }

}
