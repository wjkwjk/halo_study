package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.repository.base.BasePostRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface SheetRepository extends BasePostRepository<Sheet> {
}
