package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.MenuDTO;
import com.wjk.halo.model.entity.Menu;
import com.wjk.halo.model.params.MenuParam;
import com.wjk.halo.model.vo.MenuVO;
import com.wjk.halo.repository.MenuRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.MenuService;
import com.wjk.halo.service.base.AbstractCrudService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends AbstractCrudService<Menu, Integer> implements MenuService {

    private final MenuRepository menuRepository;

    public MenuServiceImpl(MenuRepository menuRepository) {
        super(menuRepository);
        this.menuRepository = menuRepository;
    }

    @Override
    public @NotNull Menu create(@NotNull Menu menu) {
        return super.create(menu);
    }

    @Override
    public List<MenuDTO> listDtos(Sort sort) {
        return convertTo(listAll(sort));
    }

    @Override
    public List<MenuVO> listAsTree(Sort sort) {
        List<Menu> menus = listAll(sort);

        if (CollectionUtils.isEmpty(menus)){
            return Collections.emptyList();
        }

        MenuVO topLevelMenu = createTopLevelMenu();

        concreteTree(topLevelMenu, menus);

        return topLevelMenu.getChildren();

    }

    @Override
    public List<MenuVO> listByTeamAsTree(String team, Sort sort) {
        List<Menu> menus = menuRepository.findByTeam(team, sort);

        if (CollectionUtils.isEmpty(menus)){
            return Collections.emptyList();
        }

        MenuVO topLevelMenu = createTopLevelMenu();

        concreteTree(topLevelMenu, menus);

        return topLevelMenu.getChildren();
    }

    @Override
    public @NotNull Menu update(@NotNull Menu menu) {
        return super.update(menu);
    }

    @Override
    public Menu createBy(MenuParam menuParam) {
        return create(menuParam.convertTo());
    }

    @Override
    public List<Menu> listByParentId(Integer id) {
        return menuRepository.findByParentId(id);
    }

    @Override
    public List<String> listAllTeams() {
        return menuRepository.findAllTeams();
    }

    @NonNull
    private MenuVO createTopLevelMenu(){
        MenuVO topMenu = new MenuVO();
        topMenu.setId(0);
        topMenu.setChildren(new LinkedList<>());
        topMenu.setParentId(-1);
        return topMenu;
    }

    private void concreteTree(MenuVO parentMenu, List<Menu> menus){
        if (CollectionUtils.isEmpty(menus)){
            return;
        }
        List<Menu> children = new LinkedList<>();

        menus.forEach(menu -> {
            if (parentMenu.getId().equals(menu.getParentId())){
                children.add(menu);

                MenuVO child = new MenuVO().convertFrom(menu);

                if (parentMenu.getChildren() == null){
                    parentMenu.setChildren(new LinkedList<>());
                }
                parentMenu.getChildren().add(child);
            }
        });

        menus.removeAll(children);

        if (!CollectionUtils.isEmpty(parentMenu.getChildren())){
            parentMenu.getChildren().forEach(childMenu -> concreteTree(childMenu, menus));
        }

    }

    private List<MenuDTO> convertTo(List<Menu> menus){
        if (CollectionUtils.isEmpty(menus)){
            return Collections.emptyList();
        }
        return menus.stream()
                .map(menu -> (MenuDTO) new MenuDTO().convertFrom(menu))
                .collect(Collectors.toList());
    }

}
