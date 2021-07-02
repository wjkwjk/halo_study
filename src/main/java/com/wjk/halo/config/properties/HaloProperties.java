package com.wjk.halo.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("halo")
public class HaloProperties {
    private String cache = "memory";
}
