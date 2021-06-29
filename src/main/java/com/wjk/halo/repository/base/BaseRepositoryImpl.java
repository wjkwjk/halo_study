package com.wjk.halo.repository.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;

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
}
