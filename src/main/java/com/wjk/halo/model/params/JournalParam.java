package com.wjk.halo.model.params;

import com.wjk.halo.model.dto.base.InputConverter;
import com.wjk.halo.model.entity.Journal;
import com.wjk.halo.model.enums.JournalType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class JournalParam implements InputConverter<Journal> {

    @NotBlank(message = "内容不能为空")
    @Size(max = 511, message = "内容的字符长度不能超过 {max}")
    private String sourceContent;

    private JournalType type = JournalType.PUBLIC;
}
