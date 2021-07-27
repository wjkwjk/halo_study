package com.wjk.halo.config.properties;

import com.wjk.halo.model.enums.Mode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("halo")
public class HaloProperties {
    private String cache = "memory";

    private Mode mode = Mode.PRODUCTION;

}
