package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.BaseMetaDTO;
import com.wjk.halo.model.entity.BaseMeta;
import com.wjk.halo.repository.base.BaseMetaRepository;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.service.base.BaseMetaService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseMetaServiceImpl<META extends BaseMeta> extends AbstractCrudService<META, Long> implements BaseMetaService<META> {

    private final BaseMetaRepository<META> baseMetaRepository;

    public BaseMetaServiceImpl(BaseMetaRepository<META> baseMetaRepository){
        super(baseMetaRepository);
        this.baseMetaRepository = baseMetaRepository;
    }


    @Override
    public List<META> createOrUpdateByPostId(Integer postId, Set<META> metas) {
        removeByPostId(postId);

        if (CollectionUtils.isEmpty(metas)){
            return Collections.emptyList();
        }

        metas.forEach(postMeta -> {
            if (StringUtils.isNotEmpty(postMeta.getValue()) && StringUtils.isNotEmpty(postMeta.getKey())){
                postMeta.setPostId(postId);
                baseMetaRepository.save(postMeta);
            }
        });
        return new ArrayList<>(metas);
    }

    @Override
    public List<META> removeByPostId(@NotNull Integer postId) {
        return baseMetaRepository.deleteByPostId(postId);
    }


    @Override
    public @NotNull BaseMetaDTO convertTo(@NotNull META postmeta) {
        return new BaseMetaDTO().convertFrom(postmeta);
    }

    @Override
    public @NotNull List<BaseMetaDTO> convertTo(@NotNull List<META> postMetaList) {
        if (CollectionUtils.isEmpty(postMetaList)){
            return Collections.emptyList();
        }

        return postMetaList.stream()
                .map(this::convertTo)
                .collect(Collectors.toList());
    }
}
