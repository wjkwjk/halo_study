package com.wjk.halo.model.entity.support;

import com.wjk.halo.utils.ReflectionUtils;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;

import java.io.Serializable;

public class CustomIdGenerator extends IdentityGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor s, Object obj) {
        Object id = ReflectionUtils.getFieldValue("id", obj);
        if (id != null){
            return (Serializable) id;
        }
        return super.generate(s, obj);
    }
}
