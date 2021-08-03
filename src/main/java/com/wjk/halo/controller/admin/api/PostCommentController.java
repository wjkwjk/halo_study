package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.entity.PostComment;
import com.wjk.halo.model.enums.CommentStatus;
import com.wjk.halo.model.params.CommentQuery;
import com.wjk.halo.model.vo.BaseCommentVO;
import com.wjk.halo.model.vo.PostCommentWithPostVO;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostCommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/admin/posts/comments")
public class PostCommentController {

    private final PostCommentService postCommentService;

    private final OptionService optionService;

    public PostCommentController(PostCommentService postCommentService, OptionService optionService) {
        this.postCommentService = postCommentService;
        this.optionService = optionService;
    }

    @GetMapping
    public Page<PostCommentWithPostVO> pageBy(@PageableDefault(sort = "createTime", direction = DESC) Pageable pageable,
                                              CommentQuery commentQuery){
        Page<PostComment> commentPage = postCommentService.pageBy(commentQuery, pageable);
        return postCommentService.convertToWithPostVo(commentPage);
    }

    @GetMapping("latest")
    public List<PostCommentWithPostVO> listLatest(@RequestParam(name = "top", defaultValue = "10") int top,
                                                  @RequestParam(name = "status", required = false) CommentStatus status){
        List<PostComment> content = postCommentService.pageLatest(top, status).getContent();

        return postCommentService.convertToWithPostVo(content);
    }

    @GetMapping("{postId:\\d+}/tree_view")
    public Page<BaseCommentVO> listCommentTree(@PathVariable("postId") Integer postId,
                                               @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                               @SortDefault(sort = "createTime", direction = DESC) Sort sort){
        return postCommentService.pageVosAllBy(postId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

}
