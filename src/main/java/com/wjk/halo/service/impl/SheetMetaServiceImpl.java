package com.wjk.halo.service.impl;

import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.entity.SheetMeta;
import com.wjk.halo.repository.SheetMetaRepository;
import com.wjk.halo.repository.SheetRepository;
import com.wjk.halo.repository.base.BaseMetaRepository;
import com.wjk.halo.service.SheetMetaService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SheetMetaServiceImpl extends BaseMetaServiceImpl<SheetMeta> implements SheetMetaService {

    private final SheetMetaRepository sheetMetaRepository;

    private final SheetRepository sheetRepository;

    public SheetMetaServiceImpl(SheetMetaRepository sheetMetaRepository, SheetRepository sheetRepository) {
        super(sheetMetaRepository);
        this.sheetMetaRepository = sheetMetaRepository;
        this.sheetRepository = sheetRepository;
    }

    @Override
    public void validateTarget(@NotNull Integer sheetId) {
        sheetRepository.findById(sheetId)
                .orElseThrow(() -> new NotFoundException("查询不到该页面的信息").setErrorData(sheetId));
    }
}
