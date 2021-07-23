package com.wjk.halo.service.base;

import com.wjk.halo.repository.base.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
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

    @Override
    public List<DOMAIN> listAll(Sort sort) {
        return repository.findAll(sort);
    }

    @Override
    public Page<DOMAIN> listAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public DOMAIN create(DOMAIN domain) {
        return repository.save(domain);
    }

    @Override
    public List<DOMAIN> updateInBatch(Collection<DOMAIN> domains) {
        return CollectionUtils.isEmpty(domains) ? Collections.emptyList() : repository.saveAll(domains);
    }

    @Override
    public List<DOMAIN> createInBatch(Collection<DOMAIN> domains) {
        return CollectionUtils.isEmpty(domains) ? Collections.emptyList() : repository.saveAll(domains);
    }

    @Override
    public void flush() {
        repository.flush();
    }

    @Override
    public DOMAIN update(DOMAIN domain) {
        return repository.saveAndFlush(domain);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public List<DOMAIN> listAllByIds(Collection<ID> ids) {
        return CollectionUtils.isEmpty(ids) ? Collections.emptyList() : repository.findAllById(ids);

    }

    @Override
    public void removeAll(Collection<DOMAIN> domains) {
        if (CollectionUtils.isEmpty(domains)){
            log.debug(domainName + " collection is empty");
            return;
        }
        repository.deleteInBatch(domains);
    }


}
