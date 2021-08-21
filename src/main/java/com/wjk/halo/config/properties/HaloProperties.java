package com.wjk.halo.config.properties;

import com.wjk.halo.model.enums.Mode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

import static com.wjk.halo.model.support.HaloConst.*;
import static com.wjk.halo.utils.HaloUtils.ensureSuffix;

@Data
@ConfigurationProperties("halo")
public class HaloProperties {

    private boolean docDisabled = true;

    private String cache = "memory";

    //用来定义不同的模式：测试、生产等
    //通过设置注解的属性，可以控制哪些api在哪些模式下使用
    private Mode mode = Mode.PRODUCTION;

    //系统的工作目录
    //返回的目录为：用户根目录/.halo/
    private String workDir = ensureSuffix(USER_HOME, FILE_SEPARATOR) + ".halo" + FILE_SEPARATOR;

    private Duration downloadTimeout = Duration.ofSeconds(30);

    private boolean productionEnv = true;

    private boolean authEnabled = true;

    private String backupDir = ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-backup" + FILE_SEPARATOR;

    private String dataExportDir = ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-data-export" + FILE_SEPARATOR;

    private String adminPath = "admin";

    private String uploadUrlPrefix = "upload";
}
