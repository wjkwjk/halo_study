package com.wjk.halo.config.properties;

import com.wjk.halo.model.enums.Mode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.wjk.halo.model.support.HaloConst.*;
import static com.wjk.halo.utils.HaloUtils.ensureSuffix;

@Data
@ConfigurationProperties("halo")
public class HaloProperties {
    private String cache = "memory";

    private Mode mode = Mode.PRODUCTION;

    private String workDir = ensureSuffix(USER_HOME, FILE_SEPARATOR) + ".halo" + FILE_SEPARATOR;

}
