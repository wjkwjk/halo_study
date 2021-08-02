package com.wjk.halo.controller.admin.api;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.vo.SheetDetailVO;
import com.wjk.halo.model.vo.SheetListVO;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.SheetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



}
