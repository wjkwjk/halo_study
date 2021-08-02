package com.wjk.halo.service.impl;

import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.entity.SheetMeta;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.vo.SheetDetailVO;
import com.wjk.halo.model.vo.SheetListVO;
import com.wjk.halo.repository.SheetRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.SheetCommentService;
import com.wjk.halo.service.SheetMetaService;
import com.wjk.halo.service.SheetService;
import com.wjk.halo.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;

@Slf4j
@Service
public class SheetServiceImpl extends BasePostServiceImpl<Sheet> implements SheetService {

    private final SheetRepository sheetRepository;

    private final OptionService optionService;

    private final ApplicationEventPublisher eventPublisher;

    private final SheetCommentService sheetCommentService;

    private final SheetMetaService sheetMetaService;

    public SheetServiceImpl(SheetRepository sheetRepository,
                            ApplicationEventPublisher eventPublisher,
                            SheetCommentService sheetCommentService,
                            SheetMetaService sheetMetaService,
                            OptionService optionService) {
        super(sheetRepository, optionService);
        this.sheetRepository = sheetRepository;
        this.optionService = optionService;
        this.eventPublisher = eventPublisher;
        this.sheetMetaService = sheetMetaService;
        this.sheetCommentService = sheetCommentService;
    }

    @Override
    public Sheet createBy(Sheet sheet, boolean autoSave) {
        //没有则创建，否则则更新
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

    @Override
    public SheetDetailVO convertToDetailVo(Sheet sheet) {
        List<SheetMeta> metas = sheetMetaService.listBy(sheet.getId());

        return convertTo(sheet, metas);
    }

    @Override
    public Page<SheetListVO> convertToListVo(Page<Sheet> sheetPage) {
        List<Sheet> sheets = sheetPage.getContent();

        Set<Integer> sheetIds = ServiceUtils.fetchProperty(sheets, Sheet::getId);

        Map<Integer, Long> sheetCommentCountMap = sheetCommentService.countByPostIds(sheetIds);

        return sheetPage.map(sheet -> {
            SheetListVO sheetListVO = new SheetListVO().convertFrom(sheet);
            sheetListVO.setCommentCount(sheetCommentCountMap.getOrDefault(sheet.getId(), 0L));
            sheetListVO.setFullPath(buildFullPath(sheet));

            return sheetListVO;
        });
    }

    @NonNull
    private SheetDetailVO convertTo(@NonNull Sheet sheet, List<SheetMeta> metas){
        SheetDetailVO sheetDetailVO = new SheetDetailVO().convertFrom(sheet);

        Set<Long> metaIds = ServiceUtils.fetchProperty(metas, SheetMeta::getId);

        sheetDetailVO.setMetaIds(metaIds);
        sheetDetailVO.setMetas(sheetMetaService.convertTo(metas));

        if (StringUtils.isBlank(sheetDetailVO.getSummary())){
            sheetDetailVO.setSummary(generateSummary(sheet.getFormatContent()));
        }

        sheetDetailVO.setCommentCount(sheetCommentService.countByPostId(sheet.getId()));
        sheetDetailVO.setFullPath(buildFullPath(sheet));

        return sheetDetailVO;
    }

    private String buildFullPath(Sheet sheet){
        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()){
            fullPath.append(optionService.getBlogBaseUrl());
        }

        fullPath.append(URL_SEPARATOR)
                .append(optionService.getSheetPrefix())
                .append(URL_SEPARATOR)
                .append(sheet.getSlug())
                .append(optionService.getPathSuffix());

        return fullPath.toString();

    }

    @Override
    public Page<Sheet> pageBy(Pageable pageable) {
        return listAll(pageable);
    }
}
