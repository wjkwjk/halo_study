package com.wjk.halo.controller.admin.api;

import com.wjk.halo.annotation.DisableOnCondition;
import com.wjk.halo.cache.lock.CacheLock;
import com.wjk.halo.handler.theme.config.support.Group;
import com.wjk.halo.handler.theme.config.support.ThemeProperty;
import com.wjk.halo.model.params.ThemeContentParam;
import com.wjk.halo.model.support.BaseResponse;
import com.wjk.halo.model.support.ThemeFile;
import com.wjk.halo.service.ThemeService;
import com.wjk.halo.service.ThemeSettingService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    @GetMapping("activation/template/custom/sheet")
    public List<String> customSheetTemplate(){
        return themeService.listCustomTemplates(themeService.getActivatedThemeId(), ThemeService.CUSTOM_SHEET_PREFIX);
    }

    @GetMapping("activation/template/custom/post")
    public List<String> customPostTemplate(){
        return themeService.listCustomTemplates(themeService.getActivatedThemeId(), ThemeService.CUSTOM_POST_PREFIX);
    }

    @PostMapping("{themeId}/activation")
    public ThemeProperty active(@PathVariable("themeId") String themeId){
        return themeService.activateTheme(themeId);
    }

    @GetMapping("activation")
    public ThemeProperty getActivateTheme(){
        return themeService.getThemeOfNonNullBy(themeService.getActivatedThemeId());
    }

    @GetMapping("activation/configurations")
    public BaseResponse<Object> fetchConfig(){
        return BaseResponse.ok(themeService.fetchConfig(themeService.getActivatedThemeId()));
    }

    @GetMapping("{themeId}/configurations")
    public List<Group> fetchConfig(@PathVariable("themeId") String themeId){
        return themeService.fetchConfig(themeId);
    }

    @GetMapping("activation/settings")
    public Map<String, Object> listSettingsBy(){
        return themeSettingService.listAsMapBy(themeService.getActivatedThemeId());
    }

    @GetMapping("{themeId}/settings")
    public Map<String, Object> listSettingsBy(@PathVariable("themeId") String themeId){
        return themeSettingService.listAsMapBy(themeId);
    }

    @PostMapping("activation/settings")
    public void saveSettingsBy(@RequestBody Map<String, Object> settings){
        themeSettingService.save(settings, themeService.getActivatedThemeId());
    }

    @PostMapping("{themeId}/settings")
    @CacheLock(prefix = "save_theme_setting_by_themeId")
    public void saveSettingsBy(@PathVariable("themeId") String themeId,
                               @RequestBody Map<String, Object> settings) {
        themeSettingService.save(settings, themeId);
    }

    @DeleteMapping("{themeId}")
    @DisableOnCondition
    public void deleteBy(@PathVariable("themeId") String themeId,
                         @RequestParam(value = "deleteSettings", defaultValue = "false") Boolean deleteSettings) {
        themeService.deleteTheme(themeId, deleteSettings);
    }

    @PostMapping("upload")
    public ThemeProperty uploadTheme(@RequestPart("file") MultipartFile file) {
        return themeService.upload(file);
    }

    @PutMapping("upload/{themeId}")
    public ThemeProperty updateThemeByUpload(@PathVariable("themeId") String themeId,
                                             @RequestPart("file") MultipartFile file) {
        return themeService.update(themeId, file);
    }

    @PostMapping("fetching")
    public ThemeProperty fetchTheme(@RequestParam("uri") String uri) {
        return themeService.fetch(uri);
    }

    @PostMapping("fetchingBranches")
    public List<ThemeProperty> fetchBranches(@RequestParam("uri") String uri) {
        return themeService.fetchBranches(uri);
    }

    @PostMapping("fetchingReleases")
    public List<ThemeProperty> fetchReleases(@RequestParam("uri") String uri) {
        return themeService.fetchReleases(uri);
    }

    @GetMapping("fetchingRelease")
    public ThemeProperty fetchRelease(@RequestParam("uri") String uri, @RequestParam("tag") String tagName) {
        return themeService.fetchRelease(uri, tagName);
    }

    @GetMapping("fetchBranch")
    public ThemeProperty fetchBranch(@RequestParam("uri") String uri, @RequestParam("branch") String branchName) {
        return themeService.fetchBranch(uri, branchName);
    }

    @GetMapping("fetchLatestRelease")
    public ThemeProperty fetchLatestRelease(@RequestParam("uri") String uri) {
        return themeService.fetchLatestRelease(uri);
    }

    @PutMapping("fetching/{themeId}")
    public ThemeProperty updateThemeByFetching(@PathVariable("themeId") String themeId) {
        return themeService.update(themeId);
    }

    @PostMapping("reload")
    public void reload() {
        themeService.reload();
    }

    @GetMapping(value = "activation/template/exists")
    public BaseResponse<Boolean> exists(@RequestParam(value = "template") String template) {
        return BaseResponse.ok(themeService.templateExists(template));
    }
}
