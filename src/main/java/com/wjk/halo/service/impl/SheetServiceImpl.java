package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.repository.SheetRepository;
import com.wjk.halo.service.SheetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SheetServiceImpl extends BasePostServiceImpl<Sheet> implements SheetService {

    private final SheetRepository sheetRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final SheetCo


}
