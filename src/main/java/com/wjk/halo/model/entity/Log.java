package com.wjk.halo.model.entity;

import com.wjk.halo.model.enums.LogType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "logs", indexes = {@Index(name = "logs_create_time", columnList = "create_time")})
@ToString
@EqualsAndHashCode(callSuper = true)
public class Log extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "com.wjk.halo.model.entity.support.CustomIdGenerator")
    private Long id;

    @Column(name = "log_key", length = 1023)
    private String logKey;

    @Column(name = "type", nullable = false)
    private LogType type;

    @Column(name = "content", length = 1023, nullable = false)
    private String content;

    @Column(name = "ip_address", length = 127)
    private String ipAddress;

    @Override
    protected void prePersist() {
        super.prePersist();

        if (logKey== null){
            logKey = "";
        }

        if (ipAddress == null){
            logKey = "";
        }

    }
}
