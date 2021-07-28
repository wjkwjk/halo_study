package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.Option;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionDTO implements OutputConverter<OptionDTO, Option> {
    private String key;
    private Object value;
}
