package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.BaseCommentDTO;
import com.wjk.halo.model.entity.JournalComment;
import com.wjk.halo.model.enums.CommentStatus;
import com.wjk.halo.model.params.CommentQuery;
import com.wjk.halo.model.params.JournalCommentParam;
import com.wjk.halo.model.vo.BaseCommentVO;
import com.wjk.halo.model.vo.BaseCommentWithParentVO;
import com.wjk.halo.model.vo.JournalCommentWithJournalVO;
import com.wjk.halo.service.JournalCommentService;
import com.wjk.halo.service.OptionService;
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
@RequestMapping("/api/admin/journals/comments")
public class JournalCommentController {

    private final JournalCommentService journalCommentService;

    private final OptionService optionService;

    public JournalCommentController(JournalCommentService journalCommentService, OptionService optionService) {
        this.journalCommentService = journalCommentService;
        this.optionService = optionService;
    }

    @GetMapping
    public Page<JournalCommentWithJournalVO> pageBy(@PageableDefault(sort = "createTime", direction = DESC) Pageable pageable,
                                                    CommentQuery commentQuery){
        Page<JournalComment> journalCommentPage = journalCommentService.pageBy(commentQuery, pageable);
        return journalCommentService.convertToWithJournalVo(journalCommentPage);
    }

    @GetMapping("latest")
    public List<JournalCommentWithJournalVO> listLatest(@RequestParam(name = "top", defaultValue = "10") int top,
                                                        @RequestParam(name = "status", required = false) CommentStatus status){
        List<JournalComment> latestComments = journalCommentService.pageLatest(top, status).getContent();
        return journalCommentService.convertToWithJournalVo(latestComments);
    }

    @GetMapping("{journalId:\\d+}/tree_view")
    public Page<BaseCommentVO> listCommentTree(@PathVariable("journalId") Integer journalId,
                                               @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                               @SortDefault(sort = "createTime", direction = DESC) Sort sort){
        return journalCommentService.pageVosAllBy(journalId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{journalId:\\d+}/list_view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("journalId") Integer journalId,
                                                      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                      @SortDefault(sort = "createTime", direction = DESC) Sort sort){
        return journalCommentService.pageWithParentVoBy(journalId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @PostMapping
    public BaseCommentDTO createCommentBy(@RequestBody JournalCommentParam journalCommentParam){
        JournalComment journalComment = journalCommentService.createBy(journalCommentParam);
        return journalCommentService.convertTo(journalComment);
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    public BaseCommentDTO updateStatusBy(@PathVariable("commentId") Long commentId,
                                         @PathVariable("status") CommentStatus status){
        JournalComment updatedJournalComment = journalCommentService.updateStatus(commentId, status);
        return journalCommentService.convertTo(updatedJournalComment);
    }

    @DeleteMapping("{commentId:\\d+}")
    public BaseCommentDTO deleteBy(@PathVariable("commentId") Long commentId){
        JournalComment deletedJournalComment = journalCommentService.removeById(commentId);
        return journalCommentService.convertTo(deletedJournalComment);
    }

}
