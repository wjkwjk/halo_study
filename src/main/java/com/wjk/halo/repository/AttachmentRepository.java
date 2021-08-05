package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.model.enums.AttachmentType;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;

public interface AttachmentRepository extends BaseRepository<Attachment, Integer>, JpaSpecificationExecutor<Attachment> {
    long countByPath(@NonNull String path);

    @Query(value = "select distinct a.mediaType from Attachment a")
    List<String> findAllMediaType();

    @Query(value = "select distinct a.type from Attachment a")
    List<AttachmentType> findAllType();
}
