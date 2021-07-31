package com.wjk.halo.repository.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;

@Slf4j
public class BaseRepositoryImpl<DOMAIN, ID> extends SimpleJpaRepository<DOMAIN, ID> implements BaseRepository<DOMAIN, ID> {

    private final JpaEntityInformation<DOMAIN, ID> entityInformation;
    private final EntityManager entityManager;


    public BaseRepositoryImpl(JpaEntityInformation<DOMAIN, ID> entityInformation, EntityManager entityManager){
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {

    }

    @Override
    @Transactional
    public long deleteByIdIn(Collection<ID> ids) {
        log.debug("Customized deleteByIdIn method was invoked");

        List<DOMAIN> domains = findAllById(ids);

        deleteInBatch(domains);

        return domains.size();
    }
}
