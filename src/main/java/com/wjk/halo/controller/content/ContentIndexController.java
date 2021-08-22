package com.wjk.halo.controller.content;

import com.wjk.halo.controller.content.model.PostModel;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.enums.PostPermalinkType;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@Slf4j
@Controller
@RequestMapping
public class ContentIndexController {

    private final PostService postService;

    private final OptionService optionServicel;

    private final PostModel postModel;

    public ContentIndexController(PostService postService, OptionService optionServicel, PostModel postModel) {
        this.postService = postService;
        this.optionServicel = optionServicel;
        this.postModel = postModel;
    }

    @GetMapping
    public String index(Integer p, String token, Model model){
        PostPermalinkType permalinkType = optionServicel.getPostPermalinkType();

        if (PostPermalinkType.ID.equals(permalinkType) && !Objects.isNull(p)){
            Post post = postService.getById(p);
            return postModel.content(post, token, model);
        }
        return this.index(model, 1);
    }

    @GetMapping(value = "page/{page}")
    public String index(Model model,
                        @PathVariable(value = "page") Integer page) {
        return postModel.list(page, model);
    }

}
