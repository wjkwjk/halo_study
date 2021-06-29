package com.wjk.halo.service.base;

import com.wjk.halo.repository.base.BaseRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractCrudService<DOMAIN, ID> implements CrudService<DOMAIN, ID> {

    private final String domainName;
    private final BaseRepository<DOMAIN, ID> repository;

    protected AbstractCrudService(BaseRepository<DOMAIN, ID> repository){
        this.repository = repository;
        Class<DOMAIN> domainClass = (Class<DOMAIN>) fetchType(0);
        domainName = domainClass.getSimpleName();
    }

    private Type fetchType(int index){
        return ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[index];
    }

}
