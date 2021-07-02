package com.wjk.halo.model.entity;

import com.wjk.halo.model.enums.MFAType;
import com.wjk.halo.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "users")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", length = 127)
    private String email;

    @Column(name = "avatar", length = 1023)
    private String avatar;

    @Column(name = "description", length = 1023)
    private String description;

    @Column(name = "expire_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireTime;

    @Column(name = "mfa_type", nullable = false)
    @ColumnDefault("0")
    private MFAType mfaType;

    @Column(name = "mfa_key", length = 64)
    private String mfaKey;

    public void prePersist(){
        super.prePersist();

        if (email==null){
            email = "";
        }

        if (avatar == null){
            avatar = "";
        }

        if (description == null){
            description = "";
        }

        if (expireTime == null){
            expireTime = DateUtils.now();
        }


    }
}
