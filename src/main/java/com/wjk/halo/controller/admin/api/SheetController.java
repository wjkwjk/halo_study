package com.wjk.halo.controller.admin.api;

import cn.hutool.core.util.IdUtil;
import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.model.dto.IndependentSheetDTO;
import com.wjk.halo.model.dto.post.BasePostDetailDTO;
import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.params.PostContentParam;
import com.wjk.halo.model.params.SheetParam;
import com.wjk.halo.model.vo.SheetDetailVO;
import com.wjk.halo.model.vo.SheetListVO;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.SheetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/admin/sheets")
public class SheetController {

    private final SheetService sheetService;

    private final AbstractStringCacheStore cacheStore;

    private final OptionService optionService;

    public SheetController(SheetService sheetService,
                           AbstractStringCacheStore cacheStore,
                           OptionService optionService) {
        this.sheetService = sheetService;
        this.cacheStore = cacheStore;
        this.optionService = optionService;
    }

    @GetMapping("{sheetId:\\d+}")
    public SheetDetailVO getBy(@PathVariable("sheetId") Integer sheetId){
        Sheet sheet = sheetService.getById(sheetId);
        return sheetService.convertToDetailVo(sheet);
    }

    @GetMapping
    public Page<SheetListVO> pageBy(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC)Pageable pageable){
        Page<Sheet> sheetPage = sheetService.pageBy(pageable);
        return sheetService.convertToListVo(sheetPage);
    }

    @GetMapping("independent")
    public List<IndependentSheetDTO> independentSheets(){
        return sheetService.listIndependentSheets();
    }

    @PostMapping
    public SheetDetailVO createBy(@RequestBody @Valid SheetParam sheetParam,
                                  @RequestParam(value = "autoSave", required = false, defaultValue = "false") Boolean autoSave){
        Sheet sheet = sheetService.createBy(sheetParam.convertTo(), sheetParam.getSheetMetas(), autoSave);
        return sheetService.convertToDetailVo(sheet);
    }

    @PutMapping("{sheetId:\\d+}")
    public SheetDetailVO updateBy(@PathVariable("sheetId") Integer sheetId,
                                  @RequestBody @Valid SheetParam sheetParam,
                                  @RequestParam(value = "autoSave", required = false, defaultValue = "false") Boolean autoSave){
        Sheet sheetToUpdate = sheetService.getById(sheetId);

        sheetParam.update(sheetToUpdate);

        Sheet sheet = sheetService.updateBy(sheetToUpdate, sheetParam.getSheetMetas(), autoSave);

        return sheetService.convertToDetailVo(sheet);
    }

    @PutMapping("{sheetId:\\d+}/{status}")
    public void updateStatusBy(@PathVariable("sheetId") Integer sheetId,
                               @PathVariable("status")PostStatus status){
        Sheet sheet = sheetService.getById(sheetId);

        sheet.setStatus(status);

        sheetService.update(sheet);
    }

    @PutMapping("{sheetId:\\d+}/status/draft/content")
    public BasePostDetailDTO updateDraftBy(@PathVariable("sheetId") Integer sheetId,
                                           @RequestBody PostContentParam contentParam){
        Sheet sheet = sheetService.updateDraftContent(contentParam.getContent(), sheetId);

        return new BasePostDetailDTO().convertFrom(sheet);
    }

    @DeleteMapping("{sheetId:\\d+}")
    public SheetDetailVO deleteBy(@PathVariable("sheetId") Integer sheetId){
        Sheet sheet = sheetService.removeById(sheetId);
        return sheetService.convertToDetailVo(sheet);
    }

    public String preview(@PathVariable("sheetId") Integer sheetId) throws UnsupportedEncodingException{
        Sheet sheet = sheetService.getById(sheetId);

        sheet.setSlug(URLEncoder.encode(sheet.getSlug(), StandardCharsets.UTF_8.name()));

        BasePostMinimalDTO sheetMinimalDTO = sheetService.convertToMinimal(sheet);

        String token = IdUtil.simpleUUID();

        cacheStore.putAny(token, token, 10, TimeUnit.MINUTES);

        StringBuilder previewUrl = new StringBuilder();

        if (!optionService.isEnabledAbsolutePath()){
            previewUrl.append(optionService.getBlogBaseUrl());
        }

        previewUrl.append(sheetMinimalDTO.getFullPath())
                .append("?token=")
                .append(token);

        return previewUrl.toString();

    }

}

