package com.wjk.halo.service;

import com.wjk.halo.model.dto.MenuDTO;
import com.wjk.halo.model.entity.Menu;
import com.wjk.halo.model.params.MenuParam;
import com.wjk.halo.model.vo.MenuVO;
import com.wjk.halo.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface MenuService extends CrudService<Menu, Integer> {

    @NonNull
    List<MenuDTO> listDtos(@NonNull Sort sort);

    List<MenuVO> listAsTree(@NonNull Sort sort);

    List<MenuVO> listByTeamAsTree(@NonNull String team, Sort sort);

    @NonNull
    Menu createBy(@NonNull MenuParam menuParam);

    List<Menu> listByParentId(@NonNull Integer id);

    List<String> listAllTeams();
}
