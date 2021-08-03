package com.wjk.halo.service.base;

import com.wjk.halo.model.params.OptionParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CrudService<DOMAIN, ID> {

    @NonNull
    List<DOMAIN> listAll();

    @NonNull
    List<DOMAIN> listAll(@NonNull Sort sort);

    @NonNull
    Page<DOMAIN> listAll(@NonNull Pageable pageable);

    @NonNull
    @Transactional
    DOMAIN create(@NonNull DOMAIN domain);

    @NonNull
    @Transactional
    List<DOMAIN> updateInBatch(@NonNull Collection<DOMAIN> domains);

    @NonNull
    @Transactional
    List<DOMAIN> createInBatch(@NonNull Collection<DOMAIN> domains);

    void flush();

    @NonNull
    @Transactional
    DOMAIN update(@NonNull DOMAIN domain);

    long count();

    @NonNull
    List<DOMAIN> listAllByIds(@Nullable Collection<ID> ids);


    @Transactional
    void removeAll(@NonNull Collection<DOMAIN> domains);

    @Transactional
    void removeAll();

    boolean existById(@NonNull ID id);

    void mustExistById(@NonNull ID id);

    @NonNull
    DOMAIN getById(@NonNull ID id);

    @NonNull
    Optional<DOMAIN> fetchById(@NonNull ID id);

    @NonNull
    @Transactional
    DOMAIN removeById(@NonNull ID id);

    @Transactional
    void remove(@NonNull DOMAIN domain);

    @Transactional
    void removeInBatch(@NonNull Collection<ID> ids);

}
