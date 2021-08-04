package com.wjk.halo.service;

import com.wjk.halo.model.entity.SheetComment;
import com.wjk.halo.model.vo.SheetCommentWithSheetVO;
import com.wjk.halo.service.base.BaseCommentService;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface SheetCommentService extends BaseCommentService<SheetComment> {

    @NonNull
    Page<SheetCommentWithSheetVO> convertToWithSheetVo(@NonNull Page<SheetComment> sheetCommentPage);

    @NonNull
    List<SheetCommentWithSheetVO> convertToWithSheetVo(@Nullable List<SheetComment> sheetComments);

    @NonNull
    SheetCommentWithSheetVO convertToWithSheetVo(@NonNull SheetComment comment);

}
