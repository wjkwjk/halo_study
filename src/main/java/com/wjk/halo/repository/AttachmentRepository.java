package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AttachmentRepository extends BaseRepository<Attachment, Integer>, JpaSpecificationExecutor<Attachment> {
}
