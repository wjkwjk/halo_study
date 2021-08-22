package com.wjk.halo.service.impl;

import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.dto.IndependentSheetDTO;
import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.entity.SheetComment;
import com.wjk.halo.model.entity.SheetMeta;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.vo.SheetDetailVO;
import com.wjk.halo.model.vo.SheetListVO;
import com.wjk.halo.repository.SheetRepository;
import com.wjk.halo.service.*;
import com.wjk.halo.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;

@Slf4j
@Service
public class SheetServiceImpl extends BasePostServiceImpl<Sheet> implements SheetService {

    private final SheetRepository sheetRepository;

    private final OptionService optionService;

    private final ApplicationEventPublisher eventPublisher;

    private final SheetCommentService sheetCommentService;

    private final SheetMetaService sheetMetaService;

    private final ThemeService themeService;

    public SheetServiceImpl(SheetRepository sheetRepository,
                            ApplicationEventPublisher eventPublisher,
                            SheetCommentService sheetCommentService,
                            SheetMetaService sheetMetaService,
                            ThemeService themeService,
                            OptionService optionService) {
        super(sheetRepository, optionService);
        this.sheetRepository = sheetRepository;
        this.optionService = optionService;
        this.eventPublisher = eventPublisher;
        this.sheetMetaService = sheetMetaService;
        this.sheetCommentService = sheetCommentService;
        this.themeService = themeService;
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

    @Override
    public List<IndependentSheetDTO> listIndependentSheets() {
        String context = (optionService.isEnabledAbsolutePath() ? optionService.getBlogBaseUrl() : "") + "/";

        IndependentSheetDTO linkSheet = new IndependentSheetDTO();
        linkSheet.setId(1);
        linkSheet.setTitle("友情链接");
        linkSheet.setFullPath(context + optionService.getLinksPrefix());
        linkSheet.setRouteName("LinkList");
        linkSheet.setAvailable(themeService.templateExists("links.ftl"));

        IndependentSheetDTO photoSheet = new IndependentSheetDTO();
        photoSheet.setId(2);
        photoSheet.setTitle("图库页面");
        photoSheet.setFullPath(context + optionService.getPhotosPrefix());
        photoSheet.setRouteName("PhotoList");
        photoSheet.setAvailable(themeService.templateExists("photos.ftl"));

        IndependentSheetDTO journalSheet = new IndependentSheetDTO();
        journalSheet.setId(3);
        journalSheet.setTitle("日志页面");
        journalSheet.setFullPath(context + optionService.getJournalsPrefix());
        journalSheet.setRouteName("JournalList");
        journalSheet.setAvailable(themeService.templateExists("journals.ftl"));

        return Arrays.asList(linkSheet, photoSheet, journalSheet);

    }

    @Override
    public Sheet updateBy(Sheet sheet, Set<SheetMeta> metas, boolean autoSave) {
        Sheet updatedSheet = createOrUpdateBy(sheet);

        List<SheetMeta> sheetMetaList = sheetMetaService.createOrUpdateByPostId(updatedSheet.getId(), metas);
        log.debug("Created sheet metas: [{}]", sheetMetaList);

        if (!autoSave){
            LogEvent logEvent = new LogEvent(this, updatedSheet.getId().toString(), LogType.SHEET_EDITED, updatedSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return updatedSheet;

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

    @Override
    public Sheet getBy(PostStatus status, String slug) {
        Optional<Sheet> postOptional = sheetRepository.getBySlugAndStatus(slug, status);
        return postOptional.orElseThrow(() -> new NotFoundException("查询不到该页面的信息").setErrorData(slug));
    }

    @Override
    public Sheet removeById(Integer id) {
        // Remove sheet metas
        List<SheetMeta> metas = sheetMetaService.removeByPostId(id);
        log.debug("Removed sheet metas: [{}]", metas);

        // Remove sheet comments
        List<SheetComment> sheetComments = sheetCommentService.removeByPostId(id);
        log.debug("Removed sheet comments: [{}]", sheetComments);

        Sheet sheet = super.removeById(id);

        // Log it
        eventPublisher.publishEvent(new LogEvent(this, id.toString(), LogType.SHEET_DELETED, sheet.getTitle()));

        return sheet;
    }
}
