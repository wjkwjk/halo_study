package com.wjk.halo.service;

import com.wjk.halo.model.dto.LinkDTO;
import com.wjk.halo.model.entity.Link;
import com.wjk.halo.model.params.LinkParam;
import com.wjk.halo.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.List;

public interface LinkService extends CrudService<Link, Integer> {

    @NonNull
    List<LinkDTO> listDtos(@NonNull Sort sort);

    @NonNull
    Link createBy(@NonNull LinkParam linkParam);

    boolean existByName(String name);

    boolean existByUrl(String url);

    @NonNull
    Link updateBy(Integer id, @NonNull LinkParam linkParam);

    @NonNull
    List<Link> listAllByRandom();

    List<String> listAllTeams();
}
