package com.bnpaper.agento.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "agento.storage")
public class StorageProperties {
    private String localPath = System.getProperty("java.io.tmpdir") + "/agento-exports";
}
