package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.Journal;
import com.wjk.halo.model.enums.JournalType;
import lombok.Data;

import java.util.Date;

@Data
public class JournalDTO implements OutputConverter<JournalDTO, Journal> {
    private Integer id;

    private String sourceContent;

    private String content;

    private Long likes;

    private Date createTime;

    private JournalType type;
}
