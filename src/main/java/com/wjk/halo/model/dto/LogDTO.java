package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.Log;
import com.wjk.halo.model.enums.LogType;

import java.util.Date;

public class LogDTO implements OutputConverter<LogDTO, Log> {
    private Long id;
    private String logKey;
    private LogType type;
    private String content;
    private String ipAddress;
    private Date createTime;
}
