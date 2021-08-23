package com.wjk.halo.controller.content;

import com.wjk.halo.model.dto.CategoryDTO;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.service.CategoryService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostCategoryService;
import com.wjk.halo.service.PostService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Controller
public class ContentFeedController {

    private final static String UTF_8_SUFFIX = ";charset=UTF-8";

    private final static String XML_INVALID_CHAR = "[\\x00-\\x1F\\x7F]";

    private final static String XML_MEDIA_TYPE = MediaType.APPLICATION_XML_VALUE + UTF_8_SUFFIX;

    private final PostService postService;

    private final CategoryService categoryService;

    private final PostCategoryService postCategoryService;

    private final OptionService optionService;

    private final FreeMarkerConfigurer freeMarker;

    public ContentFeedController(PostService postService, CategoryService categoryService, PostCategoryService postCategoryService, OptionService optionService, FreeMarkerConfigurer freeMarker) {
        this.postService = postService;
        this.categoryService = categoryService;
        this.postCategoryService = postCategoryService;
        this.optionService = optionService;
        this.freeMarker = freeMarker;
    }

    @GetMapping(value = {"feed", "feed.xml", "rss", "rss.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String feed(Model model) throws IOException, TemplateException{
        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getRssPageSize())));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/rss.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    @GetMapping(value = {"feed/categories/{slug}", "feed/categories/{slug}.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String feed(Model model, @PathVariable(name = "slug") String slug) throws IOException, TemplateException{
        Category category = categoryService.getBySlugOfNonNull(slug);
        CategoryDTO categoryDTO = categoryService.convertTo(category);
        model.addAttribute("category", categoryDTO);
        model.addAttribute("posts", buildCategoryPosts(buildPostPageable(optionService.getRssPageSize()), categoryDTO));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/rss.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    @GetMapping(value = {"atom", "atom.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String atom(Model model) throws IOException, TemplateException{
        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getRssPageSize())));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/atom.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    @GetMapping(value = {"atom/categories/{slug}", "atom/categories/{slug}.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String atom(Model model, @PathVariable(name = "slug") String slug) throws IOException, TemplateException{
        Category category = categoryService.getBySlugOfNonNull(slug);
        CategoryDTO categoryDTO = categoryService.convertTo(category);
        model.addAttribute("category", categoryDTO);
        model.addAttribute("posts", buildCategoryPosts(buildPostPageable(optionService.getRssPageSize()), categoryDTO));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/atom.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    @GetMapping(value = {"sitemap", "sitemap.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String sitemapXml(Model model,
                             @PageableDefault(size = Integer.MAX_VALUE, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) throws IOException, TemplateException{
        model.addAttribute("posts", buildPosts(pageable));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/sitemap_xml.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    @GetMapping(value = "sitemap.html")
    public String sitemapHtml(Model model,
                              @PageableDefault(size = Integer.MAX_VALUE, sort = "createTime", direction = DESC) Pageable pageable) {
        model.addAttribute("posts", buildPosts(pageable));
        return "common/web/sitemap_html";
    }

    @GetMapping(value = "robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots(Model model) throws IOException, TemplateException{
        Template template = freeMarker.getConfiguration().getTemplate("common/web/robots.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    private List<PostDetailVO> buildPosts(@NonNull Pageable pageable){
        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostDetailVO> posts = postService.convertToDetailVo(postPage);
        posts.getContent().forEach(postDetailVO -> {
            postDetailVO.setFormatContent(RegExUtils.replaceAll(postDetailVO.getFormatContent(), XML_INVALID_CHAR, ""));
            postDetailVO.setSummary(RegExUtils.replaceAll(postDetailVO.getSummary(), XML_INVALID_CHAR, ""));
        });
        return posts.getContent();
    }


    @NonNull
    private Pageable buildPostPageable(int size){
        return PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createTime"));
    }


    private List<PostDetailVO> buildCategoryPosts(@NonNull Pageable pageable, @NonNull CategoryDTO category){
        Page<Post> postPage = postCategoryService.pagePostBy(category.getId(), PostStatus.PUBLISHED, pageable);
        Page<PostDetailVO> posts = postService.convertToDetailVo(postPage);
        posts.getContent().forEach(postDetailVO -> {
            postDetailVO.setFormatContent(RegExUtils.replaceAll(postDetailVO.getFormatContent(), XML_INVALID_CHAR, ""));
            postDetailVO.setSummary(RegExUtils.replaceAll(postDetailVO.getSummary(), XML_INVALID_CHAR, ""));
        });
        return posts.getContent();
    }
}
