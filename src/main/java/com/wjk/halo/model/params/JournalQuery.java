package com.wjk.halo.model.params;

import com.wjk.halo.model.enums.JournalType;
import lombok.Data;

@Data
public class JournalQuery {
    private String keyword;

    private JournalType type;
}
