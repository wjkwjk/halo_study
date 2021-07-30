package com.wjk.halo.controller.admin.api;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.dto.post.BasePostSimpleDTO;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.params.PostQuery;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/posts")
public class PostController {

    private final PostService postService;
    private final AbstractStringCacheStore cacheStore;
    private final OptionService optionService;

    public PostController(PostService postService,
                          AbstractStringCacheStore cacheStore,
                          OptionService optionService) {
        this.postService = postService;
        this.cacheStore = cacheStore;
        this.optionService = optionService;
    }

    @GetMapping
    public Page<? extends BasePostSimpleDTO> pageBy(@PageableDefault(sort = {"topPriority", "createTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                                                    PostQuery postQuery,
                                                    @RequestParam(value = "more", defaultValue = "true") Boolean more){
        Page<Post> postPage = postService.pageBy(postQuery, pageable);
        if (more){
            return postService.convertToListVo(postPage);
        }
        return postService.convertToSimple(postPage);
    }

    @GetMapping("latest")
    public List<BasePostMinimalDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top){
        return postService.convertToMinimal(postService.pageLatest(top).getContent());
    }

    @GetMapping("status/{status}")
    public Page<? extends BasePostSimpleDTO> pageByStatus(@PathVariable(name = "status") PostStatus status,
                                                          @RequestParam(value = "more", required = false, defaultValue = "false") Boolean more,
                                                          @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable){
        Page<Post> posts = postService.pageBy(status, pageable);

        if (more){
            return postService.convertToListVo(posts);
        }
        return postService.convertToSimple(posts);
    }

    @GetMapping("{postId:\\d+}")
    public PostDetailVO getBy(@PathVariable("postId") Integer postId){
        Post post = postService.getById(postId);
        return postService.convertToDetailVo(post);
    }


}
