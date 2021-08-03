package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatisticWithUserDTO extends StatisticDTO implements OutputConverter<StatisticWithUserDTO, StatisticDTO> {
    private UserDTO user;
}
