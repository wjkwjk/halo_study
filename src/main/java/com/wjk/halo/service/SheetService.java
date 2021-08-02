package com.wjk.halo.service;

import com.wjk.halo.model.dto.IndependentSheetDTO;
import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.entity.SheetMeta;
import com.wjk.halo.model.vo.SheetDetailVO;
import com.wjk.halo.model.vo.SheetListVO;
import com.wjk.halo.service.base.BasePostService;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

public interface SheetService extends BasePostService<Sheet> {

    @NonNull
    Sheet createBy(@NonNull Sheet sheet, boolean autoSave);

    Sheet createBy(@NonNull Sheet sheet, Set<SheetMeta> metas, boolean autoSave);

    @NonNull
    SheetDetailVO convertToDetailVo(@NonNull Sheet sheet);

    @NonNull
    Page<SheetListVO> convertToListVo(@NonNull Page<Sheet> sheetPage);

    @NonNull
    List<IndependentSheetDTO> listIndependentSheets();

    Sheet updateBy(@NonNull Sheet sheet, Set<SheetMeta> metas, boolean autoSave);


}
