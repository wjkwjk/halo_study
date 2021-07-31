package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Menu;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;

public interface MenuRepository extends BaseRepository<Menu, Integer> {
    List<Menu> findByTeam(@NonNull String team, Sort sort);

    List<Menu> findByParentId(@NonNull Integer id);

    @Query(value = "select distinct a.team from Menu a")
    List<String> findAllTeams();
}
