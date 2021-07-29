package com.wjk.halo.controller.admin.api;

import cn.hutool.crypto.symmetric.DES;
import com.wjk.halo.annotation.DisableOnCondition;
import com.wjk.halo.model.dto.OptionDTO;
import com.wjk.halo.model.dto.OptionSimpleDTO;
import com.wjk.halo.model.entity.Option;
import com.wjk.halo.model.params.OptionParam;
import com.wjk.halo.model.params.OptionQuery;
import com.wjk.halo.service.OptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/options")
public class OptionController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    @GetMapping
    public List<OptionDTO> listAll(){
        return optionService.listDtos();
    }

    @PostMapping("saving")
    @DisableOnCondition
    public void saveOptions(@Valid @RequestBody List<OptionParam> optionParams){
        optionService.save(optionParams);
    }

    @GetMapping("map_view")
    public Map<String, Object> listAllWithMapView(){
        return optionService.listOptions();
    }

    @PostMapping("map_view/keys")
    public Map<String, Object> listAllWithMapView(@RequestBody List<String> keys){
        return optionService.listOptions(keys);
    }

    @GetMapping("list_view")
    public Page<OptionSimpleDTO> listAllWithListView(@PageableDefault(sort = "updateTime", direction = Sort.Direction.DESC)Pageable pageable,
                                                     OptionQuery optionQuery){
        return optionService.pageDtosBy(pageable, optionQuery);
    }

    @GetMapping("{id:\\d+}")
    public OptionSimpleDTO getBy(@PathVariable("id") Integer id){
        Option option = optionService.getById(id);
        return optionService.convertToDto(option);
    }

    @PostMapping
    @DisableOnCondition
    public void createBy(@RequestBody @Valid OptionParam optionParam){
        optionService.save(optionParam);
    }

    @PutMapping("{optionId:\\d+}")
    @DisableOnCondition
    public void updateBy(@PathVariable("optionId") Integer optionId,
                         @RequestBody @Valid OptionParam optionParam){
        optionService.update(optionId, optionParam);
    }

    @DeleteMapping("{optionId:\\d+}")
    @DisableOnCondition
    public void deletePermanently(@PathVariable("optionId") Integer optionId){
        optionService.removePermanently(optionId);
    }

    @PostMapping("map_view/saving")
    @DisableOnCondition
    public void saveOptionsWithMapView(@RequestBody Map<String, Object> optionMap){
        optionService.save(optionMap);
    }

}
