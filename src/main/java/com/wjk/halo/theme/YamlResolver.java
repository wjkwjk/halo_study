package com.wjk.halo.theme;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.NonNull;

public enum YamlResolver {

    INSTANCE;

    private final ObjectMapper yamlMapper;

    YamlResolver() {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @NonNull
    public ObjectMapper getYamlMapper(){
        return yamlMapper;
    }

}
