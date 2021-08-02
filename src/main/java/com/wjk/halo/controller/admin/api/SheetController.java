package com.wjk.halo.controller.admin.api;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.model.dto.IndependentSheetDTO;
import com.wjk.halo.model.entity.Sheet;
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
import java.util.List;

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
}

