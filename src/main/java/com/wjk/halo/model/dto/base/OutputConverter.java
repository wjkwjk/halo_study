package com.wjk.halo.model.dto.base;

import org.springframework.lang.NonNull;

import static com.wjk.halo.utils.BeanUtils.updateProperties;

public interface OutputConverter<DTO extends OutputConverter<DTO, DOMAIN>, DOMAIN>{
    @NonNull
    default <T extends DTO> T convertFrom(@NonNull DOMAIN domain){
        updateProperties(domain, this);
        return (T) this;
    }
}
