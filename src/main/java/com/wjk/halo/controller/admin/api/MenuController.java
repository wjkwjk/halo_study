package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.MenuDTO;
import com.wjk.halo.model.dto.base.InputConverter;
import com.wjk.halo.model.entity.Menu;
import com.wjk.halo.model.params.MenuParam;
import com.wjk.halo.model.vo.MenuVO;
import com.wjk.halo.service.MenuService;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuDTO> listAll(@SortDefault(sort = "team", direction = Sort.Direction.DESC) Sort sort){
        return menuService.listDtos(sort.and(Sort.by(Sort.Direction.ASC, "priority")));
    }

    @GetMapping("tree_view")
    public List<MenuVO> listAsTree(@SortDefault(sort = "team", direction = Sort.Direction.DESC) Sort sort){
        return menuService.listAsTree(sort.and(Sort.by(Sort.Direction.ASC, "priority")));
    }

    @GetMapping("team/tree_view")
    public List<MenuVO> listDefaultsAsTreeByTeam(@SortDefault(sort = "priority", direction = Sort.Direction.ASC) Sort sort, @RequestParam(name = "team") String team){
        return menuService.listByTeamAsTree(team, sort);
    }

    @GetMapping("{menuId:\\d+}")
    public MenuDTO getBy(@PathVariable("menuId") Integer menuId){
        return new MenuDTO().convertFrom(menuService.getById(menuId));
    }

    @PostMapping
    public MenuDTO createBy(@RequestBody @Valid MenuParam menuParam){
        return new MenuDTO().convertFrom(menuService.createBy(menuParam));
    }

    @PostMapping("/batch")
    public List<MenuDTO> createBatchBy(@RequestBody @Valid List<MenuParam> menuParams){
        List<Menu> menus = menuParams
                .stream()
                .map(InputConverter::convertTo)
                .collect(Collectors.toList());
        return menuService.createInBatch(menus).stream()
                .map(menu -> (MenuDTO) new MenuDTO().convertFrom(menu))
                .collect(Collectors.toList());
    }

    @PutMapping("{menuId:\\d+}")
    public MenuDTO updateBy(@PathVariable("menuId") Integer menuId, @RequestBody @Valid MenuParam menuParam){
        Menu menu = menuService.getById(menuId);

        menuParam.update(menu);

        return new MenuDTO().convertFrom(menuService.update(menu));
    }

    @PutMapping("/batch")
    public List<MenuDTO> updateBatchBy(@RequestBody @Valid List<MenuParam> menuParams){
        List<Menu> menus = menuParams
                .stream()
                .map(InputConverter::convertTo)
                .collect(Collectors.toList());
        return menuService.updateInBatch(menus).stream()
                .map(menu -> (MenuDTO) new MenuDTO().convertFrom(menu))
                .collect(Collectors.toList());
    }

    @DeleteMapping("{menuId:\\d+}")
    public MenuDTO deleteBy(@PathVariable("menuId") Integer menuId){
        List<Menu> menus = menuService.listByParentId(menuId);
        if (null != menus && menus.size() > 0){
            menus.forEach(menu -> {
                menu.setParentId(0);
                menuService.update(menu);
            });
        }
        return new MenuDTO().convertFrom(menuService.removeById(menuId));
    }

    @DeleteMapping("/batch")
    public List<MenuDTO> deleteBatchBy(@RequestBody List<Integer> menuIds){
        List<Menu> menus = menuService.listAllByIds(menuIds);
        menuService.removeInBatch(menuIds);
        return menus.stream()
                .map(menu -> (MenuDTO) new MenuDTO().convertFrom(menu))
                .collect(Collectors.toList());
    }

    @GetMapping("teams")
    public List<String> teams(){
        return menuService.listAllTeams();
    }

}
