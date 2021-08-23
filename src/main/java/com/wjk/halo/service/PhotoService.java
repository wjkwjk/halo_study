package com.wjk.halo.service;

import com.wjk.halo.model.dto.PhotoDTO;
import com.wjk.halo.model.entity.Photo;
import com.wjk.halo.model.params.PhotoParam;
import com.wjk.halo.model.params.PhotoQuery;
import com.wjk.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.List;

public interface PhotoService extends CrudService<Photo, Integer> {

    List<PhotoDTO> listDtos(@NonNull Sort sort);

    @NonNull
    Page<PhotoDTO> pageDtosBy(@NonNull Pageable pageable, PhotoQuery photoQuery);

    @NonNull
    Photo createBy(@NonNull PhotoParam photoParam);

    List<String> listAllTeams();

    Page<PhotoDTO> pageBy(@NonNull Pageable pageable);

}
