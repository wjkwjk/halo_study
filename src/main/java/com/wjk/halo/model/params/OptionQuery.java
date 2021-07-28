package com.wjk.halo.model.params;

import com.wjk.halo.model.enums.OptionType;
import lombok.Data;

@Data
public class OptionQuery {

    private String keyword;

    private OptionType type;

}
