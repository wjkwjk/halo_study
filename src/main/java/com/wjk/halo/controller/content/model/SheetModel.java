package com.wjk.halo.controller.content.model;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.exception.ForbiddenException;
import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.entity.SheetMeta;
import com.wjk.halo.model.enums.PostEditorType;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.support.HaloConst;
import com.wjk.halo.model.vo.SheetDetailVO;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.SheetMetaService;
import com.wjk.halo.service.SheetService;
import com.wjk.halo.service.ThemeService;
import com.wjk.halo.utils.MarkdownUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;

@Component
public class SheetModel {

    private final SheetService sheetService;

    private final SheetMetaService sheetMetaService;

    private final AbstractStringCacheStore cacheStore;

    private final ThemeService themeService;

    private final OptionService optionService;

    public SheetModel(SheetService sheetService, SheetMetaService sheetMetaService, AbstractStringCacheStore cacheStore, ThemeService themeService, OptionService optionService) {
        this.sheetService = sheetService;
        this.sheetMetaService = sheetMetaService;
        this.cacheStore = cacheStore;
        this.themeService = themeService;
        this.optionService = optionService;
    }

    public String content(Sheet sheet, String token, Model model) {

        if (StringUtils.isEmpty(token)) {
            sheet = sheetService.getBy(PostStatus.PUBLISHED, sheet.getSlug());
        } else {
            // verify token
            String cachedToken = cacheStore.getAny(token, String.class).orElseThrow(() -> new ForbiddenException("您没有该页面的访问权限"));
            if (!cachedToken.equals(token)) {
                throw new ForbiddenException("您没有该页面的访问权限");
            }
            // render markdown to html when preview sheet
            if (sheet.getEditorType().equals(PostEditorType.MARKDOWN)) {
                sheet.setFormatContent(MarkdownUtils.renderHtml(sheet.getOriginalContent()));
            } else {
                sheet.setFormatContent(sheet.getOriginalContent());
            }
        }

        sheetService.publishVisitEvent(sheet.getId());

        SheetDetailVO sheetDetailVO = sheetService.convertToDetailVo(sheet);

        List<SheetMeta> metas = sheetMetaService.listBy(sheet.getId());

        // Generate meta keywords.
        if (StringUtils.isNotEmpty(sheet.getMetaKeywords())) {
            model.addAttribute("meta_keywords", sheet.getMetaKeywords());
        } else {
            model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        }

        // Generate meta description.
        if (StringUtils.isNotEmpty(sheet.getMetaDescription())) {
            model.addAttribute("meta_description", sheet.getMetaDescription());
        } else {
            model.addAttribute("meta_description", sheetService.generateDescription(sheet.getFormatContent()));
        }

        // sheet and post all can use
        model.addAttribute("sheet", sheetDetailVO);
        model.addAttribute("post", sheetDetailVO);
        model.addAttribute("is_sheet", true);
        model.addAttribute("metas", sheetMetaService.convertToMap(metas));

        if (themeService.templateExists(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate() + HaloConst.SUFFIX_FTL)) {
            return themeService.render(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate());
        }
        return themeService.render("sheet");
    }
}
