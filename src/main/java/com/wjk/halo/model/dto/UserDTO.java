package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.MFAType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@EqualsAndHashCode
public class UserDTO implements OutputConverter<UserDTO, User> {
    private Integer id;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

    private String description;

    private MFAType mfaType;

    private Date createTime;

    private Date updateTime;

}
