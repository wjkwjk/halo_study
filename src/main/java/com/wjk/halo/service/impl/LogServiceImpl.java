package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.LogDTO;
import com.wjk.halo.model.entity.Log;
import com.wjk.halo.repository.LogRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.LogService;
import com.wjk.halo.service.base.AbstractCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl extends AbstractCrudService<Log, Long> implements LogService {

    private final LogRepository logRepository;

    public LogServiceImpl(LogRepository logRepository) {
        super(logRepository);
        this.logRepository = logRepository;
    }

    @Override
    public Page<LogDTO> pageLatest(int top) {
        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "createTime"));

        return listAll(latestPageable).map(log -> new LogDTO().convertFrom(log));
    }
}
