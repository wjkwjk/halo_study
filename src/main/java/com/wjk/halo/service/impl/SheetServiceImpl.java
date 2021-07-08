package com.wjk.halo.service.impl;

import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.entity.SheetMeta;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.repository.SheetRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.SheetMetaService;
import com.wjk.halo.service.SheetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class SheetServiceImpl extends BasePostServiceImpl<Sheet> implements SheetService {

    private final SheetRepository sheetRepository;

    private final OptionService optionService;

    private final ApplicationEventPublisher eventPublisher;

    private final SheetMetaService sheetMetaService;

    public SheetServiceImpl(SheetRepository sheetRepository, ApplicationEventPublisher eventPublisher, SheetMetaService sheetMetaService, OptionService optionService) {
        super(sheetRepository, optionService);
        this.sheetRepository = sheetRepository;
        this.optionService = optionService;
        this.eventPublisher = eventPublisher;
        this.sheetMetaService = sheetMetaService;
    }

    @Override
    public Sheet createBy(Sheet sheet, boolean autoSave) {
        Sheet createSheet = createOrUpdateBy(sheet);
        if (!autoSave){
            LogEvent logEvent = new LogEvent(this, createSheet.getId().toString(), LogType.SHEET_PUBLISHED, createSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createSheet;
    }

    @Override
    public Sheet createBy(Sheet sheet, Set<SheetMeta> metas, boolean autoSave) {
        Sheet createdSheet = createOrUpdateBy(sheet);
        List<SheetMeta> sheetMetaList = sheetMetaService.createOrUpdateByPostId(sheet.getId(), metas);
        log.debug("Created sheet metas: [{}]", sheetMetaList);

        if (!autoSave){
            LogEvent logEvent = new LogEvent(this, createdSheet.getId().toString(), LogType.SHEET_PUBLISHED, createdSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdSheet;

    }
}
