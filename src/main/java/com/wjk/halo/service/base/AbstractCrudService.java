package com.wjk.halo.service.base;

import com.wjk.halo.repository.base.BaseRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class AbstractCrudService<DOMAIN, ID> implements CrudService<DOMAIN, ID> {

    private final String domainName;
    private final BaseRepository<DOMAIN, ID> repository;

    protected AbstractCrudService(BaseRepository<DOMAIN, ID> repository){
        this.repository = repository;
        //返回CrudService中的DOMAIN参数
        Class<DOMAIN> domainClass = (Class<DOMAIN>) fetchType(0);
        domainName = domainClass.getSimpleName();
    }

    //返回当前类的直接父类中的第index个参数，第一个参数即指DOMAIN，第二个参数即指ID
    private Type fetchType(int index){
        return ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[index];
    }

    @Override
    public List<DOMAIN> listAll(){
        return repository.findAll();
    }

}
