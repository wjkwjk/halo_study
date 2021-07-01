package com.wjk.halo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;

public class JsonUtils {

    public final static ObjectMapper DEFAULT_JSON_MAPPER = createDefaultJsonMapper();

    private JsonUtils(){}

    //生成ObjectMapper，用来对序列化或者反序列化
    public static ObjectMapper createDefaultJsonMapper(){
        return createDefaultJsonMapper(null);
    }

    @NonNull
    public static ObjectMapper createDefaultJsonMapper(@Nullable PropertyNamingStrategy strategy){
        ObjectMapper mapper = new ObjectMapper();
        //反序列化的时候如果多了其他属性,不抛出异常
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (strategy != null){
            mapper.setPropertyNamingStrategy(strategy);
        }
        return mapper;
    }

    /**
     *
     * @param json  String类型的json
     * @param type  要反序列化得到的对象类型
     * @param <T>
     * @return
     * @throws IOException
     */
    @NonNull
    public static <T> T jsonToObject(@NonNull String json, @NonNull Class<T> type) throws IOException{
        return jsonToObject(json, type, DEFAULT_JSON_MAPPER);
    }

    //反序列化到type类型对象
    @NonNull
    public static <T> T jsonToObject(@NonNull String json, @NonNull Class<T> type, @NonNull ObjectMapper objectMapper)throws IOException{
        return objectMapper.readValue(json, type);
    }
    //source为HashMap类型，包含所有从数据中查询到的键值对，目的是将source数据变为Json类型
    @NonNull
    public static String objectToJson(@NonNull Object source) throws JsonProcessingException{
        return objectToJson(source, DEFAULT_JSON_MAPPER);
    }

    //将source序列化到json类型
    @NonNull
    public static String objectToJson(@NonNull Object source, @NonNull ObjectMapper objectMapper) throws JsonProcessingException{
        return objectMapper.writeValueAsString(source);
    }

}
