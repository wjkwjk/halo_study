package com.wjk.halo.service;

import com.wjk.halo.model.dto.LogDTO;
import com.wjk.halo.model.entity.Log;
import com.wjk.halo.service.base.CrudService;
import org.springframework.data.domain.Page;

public interface LogService extends CrudService<Log, Long> {
    Page<LogDTO> pageLatest(int top);
}
