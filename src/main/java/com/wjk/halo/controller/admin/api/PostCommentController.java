package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.BaseCommentDTO;
import com.wjk.halo.model.entity.PostComment;
import com.wjk.halo.model.enums.CommentStatus;
import com.wjk.halo.model.params.CommentQuery;
import com.wjk.halo.model.params.PostCommentParam;
import com.wjk.halo.model.vo.BaseCommentVO;
import com.wjk.halo.model.vo.BaseCommentWithParentVO;
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

import javax.validation.Valid;
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

    @GetMapping("{postId:\\d+}/list_view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("postId") Integer postId,
                                                      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                      @SortDefault(sort = "createTime", direction = DESC) Sort sort){
        return postCommentService.pageWithParentVoBy(postId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @PostMapping
    public BaseCommentDTO createBy(@RequestBody PostCommentParam postCommentParam){
        PostComment createdPostComment = postCommentService.createBy(postCommentParam);
        return postCommentService.convertTo(createdPostComment);
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    public BaseCommentDTO updateStatusBy(@PathVariable("commentId") Long commentId,
                                         @PathVariable("status") CommentStatus status){
        PostComment updatedPostComment = postCommentService.updateStatus(commentId, status);
        return postCommentService.convertTo(updatedPostComment);
    }

    @PutMapping("status/{status}")
    public List<BaseCommentDTO> updateStatusInBatch(@PathVariable(name = "status") CommentStatus status,
                                                    @RequestBody List<Long> ids){
        List<PostComment> comments = postCommentService.updateStatusByIds(ids, status);
        return postCommentService.convertTo(comments);
    }

    @DeleteMapping("{commentId:\\d+}")
    public BaseCommentDTO deletePermanently(@PathVariable("commentId") Long commentId){
        PostComment deletedPostComment = postCommentService.removeById(commentId);
        return postCommentService.convertTo(deletedPostComment);
    }

    @DeleteMapping
    public List<PostComment> deletePermanentlyInBatch(@RequestBody List<Long> ids){
        return postCommentService.removeByIds(ids);
    }

    @GetMapping("{commentId:\\d+}")
    public PostCommentWithPostVO getBy(@PathVariable("commentId") Long commentId){
        PostComment comment = postCommentService.getById(commentId);
        return postCommentService.convertToWithPostVo(comment);
    }

    @PutMapping("{commentId:\\d+}")
    public BaseCommentDTO updateBy(@Valid @RequestBody PostCommentParam commentParam,
                                   @PathVariable("commentId") Long commentId){
        PostComment commentToUpdate = postCommentService.getById(commentId);

        commentParam.update(commentToUpdate);

        return postCommentService.convertTo(postCommentService.update(commentToUpdate));
    }

}
