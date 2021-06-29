package com.wjk.halo.model.entity;

import com.wjk.halo.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Data
@ToString
@MappedSuperclass
@EqualsAndHashCode
public class BaseEntity {

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @PrePersist
    protected void prePersist(){
        Date now = DateUtils.now();
        if (createTime==null){
            createTime=now;
        }
        if (updateTime==null){
            updateTime=now;
        }
    }

    @PreUpdate
    protected void preUpdate(){
        updateTime = new Date();
    }

    @PreRemove
    protected void preRemove(){
        updateTime = new Date();
    }

}
