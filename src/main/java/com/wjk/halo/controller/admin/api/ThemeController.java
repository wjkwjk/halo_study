package com.wjk.halo.controller.admin.api;

import com.wjk.halo.annotation.DisableOnCondition;
import com.wjk.halo.handler.theme.config.support.ThemeProperty;
import com.wjk.halo.model.params.ThemeContentParam;
import com.wjk.halo.model.support.BaseResponse;
import com.wjk.halo.model.support.ThemeFile;
import com.wjk.halo.service.ThemeService;
import com.wjk.halo.service.ThemeSettingService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/themes")
public class ThemeController {

    private final ThemeService themeService;

    private final ThemeSettingService themeSettingService;

    public ThemeController(ThemeService themeService, ThemeSettingService themeSettingService) {
        this.themeService = themeService;
        this.themeSettingService = themeSettingService;
    }

    @GetMapping("{themeId}")
    public ThemeProperty getBy(@PathVariable("themeId") String themeId){
        return themeService.getThemeOfNonNullBy(themeId);
    }

    @GetMapping
    public List<ThemeProperty> listAll(){
        return themeService.getThemes();
    }

    @GetMapping("activation/files")
    public List<ThemeFile> listFiles(){
        return themeService.listThemeFolderBy(themeService.getActivatedThemeId());
    }

    @GetMapping("files/content")
    public BaseResponse<String> getContentBy(@RequestParam(name = "path") String path){
        return BaseResponse.ok(HttpStatus.OK.getReasonPhrase(), themeService.getTemplateContent(path));
    }

    @GetMapping("{themeId}/files/content")
    public BaseResponse<String> getContentBy(@PathVariable("themeId") String themeId,
                                             @RequestParam(name = "path") String path){
        return BaseResponse.ok(HttpStatus.OK.getReasonPhrase(), themeService.getTemplateContent(themeId, path));
    }

    @PutMapping("files/content")
    @DisableOnCondition
    public void updateContentBy(@RequestBody ThemeContentParam param){
        themeService.saveTemplateContent(param.getPath(), param.getContent());
    }

    @PutMapping("{themeId}/files/content")
    @DisableOnCondition
    public void updateContentBy(@PathVariable("themeId") String themeId,
                                @RequestBody ThemeContentParam param) {
        themeService.saveTemplateContent(themeId, param.getPath(), param.getContent());
    }

    public List<String> customSheetTemplate(){
        return themeService.lis
    }

}
