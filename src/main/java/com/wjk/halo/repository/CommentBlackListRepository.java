package com.wjk.halo.repository;

import com.wjk.halo.model.entity.CommonBlackList;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentBlackListRepository extends BaseRepository<CommonBlackList, Long> {

    /**
     * 根据IP地址获取数据
     *
     * @param ipAddress
     * @return
     */
    Optional<CommonBlackList> findByIpAddress(String ipAddress);

    /**
     * Update Comment BlackList By IPAddress
     *
     * @param commentBlackList
     * @return result
     */
    @Modifying
    @Query("UPDATE CommonBlackList SET banTime=:#{#commentBlackList.banTime} WHERE ipAddress=:#{#commentBlackList.ipAddress}")
    int updateByIpAddress(@Param("commentBlackList") CommonBlackList commentBlackList);

}
