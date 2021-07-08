package com.wjk.halo.service;

import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.entity.SheetMeta;
import com.wjk.halo.service.base.BasePostService;
import org.springframework.lang.NonNull;

import java.util.Set;

public interface SheetService extends BasePostService<Sheet> {

    @NonNull
    Sheet createBy(@NonNull Sheet sheet, boolean autoSave);

    Sheet createBy(@NonNull Sheet sheet, Set<SheetMeta> metas, boolean autoSave);

}
