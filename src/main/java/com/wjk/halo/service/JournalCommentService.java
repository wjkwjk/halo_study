package com.wjk.halo.service;

import com.wjk.halo.model.entity.JournalComment;
import com.wjk.halo.model.vo.JournalCommentWithJournalVO;
import com.wjk.halo.service.base.BaseCommentService;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface JournalCommentService extends BaseCommentService<JournalComment> {

    @NonNull
    List<JournalCommentWithJournalVO> convertToWithJournalVo(@Nullable List<JournalComment> journalComments);

    @NonNull
    Page<JournalCommentWithJournalVO> convertToWithJournalVo(@NonNull Page<JournalComment> journalCommentPage);

}
