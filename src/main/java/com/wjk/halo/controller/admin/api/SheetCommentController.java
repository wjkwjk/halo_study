package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.BaseCommentDTO;
import com.wjk.halo.model.entity.SheetComment;
import com.wjk.halo.model.enums.CommentStatus;
import com.wjk.halo.model.params.CommentQuery;
import com.wjk.halo.model.params.SheetCommentParam;
import com.wjk.halo.model.vo.BaseCommentVO;
import com.wjk.halo.model.vo.BaseCommentWithParentVO;
import com.wjk.halo.model.vo.SheetCommentWithSheetVO;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.SheetCommentService;
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
@RequestMapping("/api/admin/sheets/comments")
public class SheetCommentController {

    private final SheetCommentService sheetCommentService;

    private final OptionService optionService;

    public SheetCommentController(SheetCommentService sheetCommentService, OptionService optionService) {
        this.sheetCommentService = sheetCommentService;
        this.optionService = optionService;
    }

    @GetMapping
    public Page<SheetCommentWithSheetVO> pageBy(@PageableDefault(sort = "createTime", direction = DESC) Pageable pageable,
                                                CommentQuery commentQuery){
        Page<SheetComment> sheetCommentPage = sheetCommentService.pageBy(commentQuery, pageable);
        return sheetCommentService.convertToWithSheetVo(sheetCommentPage);
    }

    @GetMapping("latest")
    public List<SheetCommentWithSheetVO> listLatest(@RequestParam(name = "top", defaultValue = "10") int top,
                                                    @RequestParam(name = "status", required = false) CommentStatus status){
        Page<SheetComment> sheetCommentPage = sheetCommentService.pageLatest(top, status);
        return sheetCommentService.convertToWithSheetVo(sheetCommentPage.getContent());
    }

    @GetMapping("{sheetId:\\d+}/tree_view")
    public Page<BaseCommentVO> listCommentTree(@PathVariable("sheetId") Integer sheetId,
                                               @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                               @SortDefault(sort = "createTime", direction = DESC) Sort sort){
        return sheetCommentService.pageVosAllBy(sheetId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{sheetId:\\d+}/list_view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("sheetId") Integer sheetId,
                                                      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                      @SortDefault(sort = "createTime", direction = DESC) Sort sort){
        return sheetCommentService.pageWithParentVoBy(sheetId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @PostMapping
    public BaseCommentDTO createBy(@RequestBody SheetCommentParam commentParam){
        SheetComment createdComment = sheetCommentService.createBy(commentParam);
        return sheetCommentService.convertTo(createdComment);
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    public BaseCommentDTO updateStatusBy(@PathVariable("commentId") Long commentId,
                                         @PathVariable("status") CommentStatus status){
        SheetComment updatedSheetComment = sheetCommentService.updateStatus(commentId, status);
        return sheetCommentService.convertTo(updatedSheetComment);
    }

    @PutMapping("status/{status}")
    public List<BaseCommentDTO> updateStatusInBatch(@PathVariable(name = "status") CommentStatus status,
                                                    @RequestBody List<Long> ids){
        List<SheetComment> comments = sheetCommentService.updateStatusByIds(ids, status);
        return sheetCommentService.convertTo(comments);
    }

    @DeleteMapping("{commentId:\\d+}")
    public BaseCommentDTO deletePermanently(@PathVariable("commentId") Long commentId){
        SheetComment deletedSheetComment = sheetCommentService.removeById(commentId);
        return sheetCommentService.convertTo(deletedSheetComment);
    }

    @DeleteMapping
    public List<SheetComment> deletePermanentlyInBatch(@RequestBody List<Long> ids){
        return sheetCommentService.removeByIds(ids);
    }

    @GetMapping("{commentId:\\d+}")
    public SheetCommentWithSheetVO getBy(@PathVariable("commentId") Long commentId){
        SheetComment comment = sheetCommentService.getById(commentId);
        return sheetCommentService.convertToWithSheetVo(comment);
    }

    public BaseCommentDTO updateBy(@Valid @RequestBody SheetCommentParam commentParam,
                                   @PathVariable("commentId") Long commentId){
        SheetComment commentToUpadte = sheetCommentService.getById(commentId);
        commentParam.update(commentToUpadte);
        return sheetCommentService.convertTo(sheetCommentService.update(commentToUpadte));
    }
}
