package com.wjk.halo.model.dto;

import com.wjk.halo.model.enums.Mode;
import lombok.Data;

@Data
public class EnvironmentDTO {
    private String database;

    private Long startTime;

    private String version;

    private Mode mode;
}
