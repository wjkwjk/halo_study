package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Link;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LinkRepository extends BaseRepository<Link, Integer> {

    @Query(value = "select distinct a.team from Link a")
    List<String> findAllTeams();

    boolean existsByNameAndIdNot(String name, Integer id);

    boolean existsByUrlAndIdNot(String url, Integer id);

}
