package com.wjk.halo.model.vo;

import com.wjk.halo.model.dto.BaseCommentDTO;
import com.wjk.halo.model.dto.JournalDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JournalCommentWithJournalVO extends BaseCommentDTO {

    private JournalDTO journal;
}
