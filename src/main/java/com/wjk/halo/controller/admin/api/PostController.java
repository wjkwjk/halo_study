package com.wjk.halo.controller.admin.api;

import cn.hutool.core.util.IdUtil;
import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.model.dto.post.BasePostDetailDTO;
import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.dto.post.BasePostSimpleDTO;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.enums.PostPermalinkType;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.params.PostContentParam;
import com.wjk.halo.model.params.PostParam;
import com.wjk.halo.model.params.PostQuery;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostService;
import javafx.geometry.Pos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    //获得最新的top个post
    @GetMapping("latest")
    public List<BasePostMinimalDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top){
        return postService.convertToMinimal(postService.pageLatest(top).getContent());
    }
    //根据status查询，基本跟pageBy一样，只不过一个是自己输入查询条件，这个是只能以status查询
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

    @PutMapping("{postId:\\d+}/likes")
    public void likes(@PathVariable("postId") Integer postId){
        postService.increaseLike(postId);
    }

    @PostMapping
    public PostDetailVO createBy(@Valid @RequestBody PostParam postParam,
                                 @RequestParam(value = "autoSave", required = false, defaultValue = "false") Boolean autoSave){
        Post post = postParam.convertTo();
        return postService.createBy(post, postParam.getTagIds(), postParam.getCategoryIds(), postParam.getPostMetas(), autoSave);
    }

    @PutMapping("{postId:\\d+}")
    public PostDetailVO updateBy(@Valid @RequestBody PostParam postParam,
                                 @PathVariable("postId") Integer postId,
                                 @RequestParam(value = "autoSave", required = false, defaultValue = "false") Boolean autoSave){
        Post postToUpdate = postService.getById(postId);

        postParam.update(postToUpdate);
        return postService.updateBy(postToUpdate, postParam.getTagIds(), postParam.getCategoryIds(), postParam.getPostMetas(), autoSave);

    }

    @PutMapping("{postId:\\d+}/status/{status}")
    public BasePostMinimalDTO updateStatusBy(
            @PathVariable("postId") Integer postId,
            @PathVariable("status") PostStatus status){
        Post post = postService.updateStatus(status, postId);

        return new BasePostMinimalDTO().convertFrom(post);
    }

    @PutMapping("status/{status}")
    public List<Post> updateStatusInBatch(@PathVariable(name = "status") PostStatus status,
                                          @RequestBody List<Integer> ids){
        return postService.updateStatusByIds(ids, status);
    }

    @PutMapping("{postId:\\d+}/status/draft/content")
    public BasePostDetailDTO updateDraftBy(
            @PathVariable("postId") Integer postId,
            @RequestBody PostContentParam contentParam){
        Post post = postService.updateDraftContent(contentParam.getContent(), postId);
        return new BasePostDetailDTO().convertFrom(post);
    }

    @DeleteMapping("{postId:\\d+}")
    public void deletePermanently(@PathVariable("postId") Integer postId){
        postService.removeById(postId);
    }

    @DeleteMapping
    public List<Post> deletePermanentlyInBatch(@RequestBody List<Integer> ids){
        return postService.removeByIds(ids);
    }

    @GetMapping(value = {"preview/{postId:\\d+}", "{postId:\\d+}/preview"})
    public String preview(@PathVariable("postId") Integer postId) throws UnsupportedEncodingException{
        Post post = postService.getById(postId);

        post.setSlug(URLEncoder.encode(post.getSlug(), StandardCharsets.UTF_8.name()));

        BasePostMinimalDTO postMinimalDTO = postService.convertToMinimal(post);

        String token = IdUtil.simpleUUID();

        cacheStore.putAny(token, token, 10, TimeUnit.MINUTES);

        StringBuilder previewUrl = new StringBuilder();

        if (!optionService.isEnabledAbsolutePath()){
            previewUrl.append(optionService.getBlogBaseUrl());
        }

        previewUrl.append(postMinimalDTO.getFullPath());

        if (optionService.getPostPermalinkType().equals(PostPermalinkType.ID)){
            previewUrl.append("&token=")
                    .append(token);
        }else {
            previewUrl.append("?token=")
                    .append(token);
        }
        return previewUrl.toString();

    }
}
