package com.wjk.halo.controller.content;

import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.vo.PostListVO;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostService;
import com.wjk.halo.service.ThemeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

@Controller
@RequestMapping(value = "/search")
public class ContentSearchController {

    private final PostService postService;

    private final OptionService optionService;

    private final ThemeService themeService;

    public ContentSearchController(PostService postService, OptionService optionService, ThemeService themeService) {
        this.postService = postService;
        this.optionService = optionService;
        this.themeService = themeService;
    }

    @GetMapping
    public String search(Model model, @RequestParam(value = "keyword") String keyword){
        return this.search(model, HtmlUtils.htmlEscape(keyword), 1, Sort.by(Sort.Direction.DESC, "createTime"));
    }

    @GetMapping(value = "page/{page}")
    public String search(Model model,
                         @RequestParam(value = "keyword") String keyword,
                         @PathVariable(value = "page") Integer page,
                         @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort){
        final Pageable pageable = PageRequest.of(page - 1, optionService.getPostPageSize(), sort);
        final Page<Post> postPage = postService.pageBy(keyword, pageable);
        final Page<PostListVO> posts = postService.convertToListVo(postPage);

        model.addAttribute("is_search", true);
        model.addAttribute("keyword", keyword);
        model.addAttribute("posts", posts);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("search");
    }

}
