package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Photo;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PhotoRepository extends BaseRepository<Photo, Integer>, JpaSpecificationExecutor<Photo> {

    List<Photo> findByTeam(String team, Sort sort);

    @Query(value = "select distinct p.team from Photo p")
    List<String> findAllTeams();

}
