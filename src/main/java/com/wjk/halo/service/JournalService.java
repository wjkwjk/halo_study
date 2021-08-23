package com.wjk.halo.service;

import com.wjk.halo.model.dto.JournalDTO;
import com.wjk.halo.model.dto.JournalWithCmtCountDTO;
import com.wjk.halo.model.entity.Journal;
import com.wjk.halo.model.enums.JournalType;
import com.wjk.halo.model.params.JournalParam;
import com.wjk.halo.model.params.JournalQuery;
import com.wjk.halo.service.base.CrudService;
import jdk.nashorn.internal.scripts.JO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface JournalService extends CrudService<Journal, Integer> {

    @NonNull
    Page<Journal> pageBy(@NonNull JournalType type, @NonNull Pageable pageable);


    @NonNull
    Page<Journal> pageBy(@NonNull JournalQuery journalQuery, @NonNull Pageable pageable);

    @NonNull
    Page<JournalWithCmtCountDTO> convertToCmtCountDto(@NonNull Page<Journal> journalPage);

    @NonNull
    List<JournalWithCmtCountDTO> convertToCmtCountDto(@Nullable List<Journal> journals);

    Page<Journal> pageLatest(int top);

    @NonNull
    Journal createBy(@NonNull JournalParam journalParam);

    @NonNull
    JournalDTO convertTo(@NonNull Journal journal);

    Journal updateBy(@NonNull Journal journal);
}
