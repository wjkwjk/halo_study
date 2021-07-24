package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.Menu;
import com.wjk.halo.repository.MenuRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.MenuService;
import com.wjk.halo.service.base.AbstractCrudService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

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
}
