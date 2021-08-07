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

    private Mode mode = Mode.PRODUCTION;

    private String workDir = ensureSuffix(USER_HOME, FILE_SEPARATOR) + ".halo" + FILE_SEPARATOR;

    private Duration downloadTimeout = Duration.ofSeconds(30);

    private boolean productionEnv = true;

    private boolean authEnabled = true;

    private String backupDir = ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-backup" + FILE_SEPARATOR;

    private String dataExportDir = ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-data-export" + FILE_SEPARATOR;

    private String adminPath = "admin";

    private String uploadUrlPrefix = "upload";
}
