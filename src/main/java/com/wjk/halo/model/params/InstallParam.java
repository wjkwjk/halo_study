package com.wjk.halo.model.params;

import com.wjk.halo.model.support.CreateCheck;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
public class InstallParam extends UserParam{

    private String locate = "zh";

    @NotBlank(message = "博客名称不能为空", groups = CreateCheck.class)
    private String title;

    private String url;

}
