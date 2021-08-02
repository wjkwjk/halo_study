package com.wjk.halo.service;

import com.wjk.halo.model.dto.OptionDTO;
import com.wjk.halo.model.dto.OptionSimpleDTO;
import com.wjk.halo.model.entity.Option;
import com.wjk.halo.model.entity.PostTag;
import com.wjk.halo.model.enums.PostPermalinkType;
import com.wjk.halo.model.params.OptionParam;
import com.wjk.halo.model.params.OptionQuery;
import com.wjk.halo.model.properties.PropertyEnum;
import com.wjk.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OptionService extends CrudService<Option, Integer> {

    String OPTIONS_KEY = "options";

    /**
     *
     * @param property  需要查找的数据的名称
     * @param propertyType  需要查找的数据的类型
     * @param defaultValue  默认类型
     * @param <T>
     * @return
     */
    <T> T getByPropertyOrDefault(@NonNull PropertyEnum property, @NonNull Class<T> propertyType, T defaultValue);

    <T> T getByPropertyOrDefault(@NonNull PropertyEnum property, @NonNull Class<T> propertyType);

    <T> Optional<T> getByProperty(@NonNull PropertyEnum property, @NonNull Class<T> propertyType);

    Optional<Object> getByProperty(@NonNull PropertyEnum property);

    @NonNull
    Optional<Object> getByKey(@NonNull String key);

    @NonNull
    @Transactional
    Map<String, Object> listOptions();

    @NonNull
    String getBlogTitle();

    @NonNull
    String getBlogBaseUrl();

    @Transactional
    void save(@Nullable Map<String, Object> options);

    @Transactional
    void save(@Nullable List<OptionParam> optionParams);

    @Transactional
    void save(@Nullable OptionParam optionParam);

    @Transactional
    void saveProperty(@NonNull PropertyEnum propertyEnum, @Nullable String value);

    @Transactional
    void saveProperties(@NonNull Map<? extends PropertyEnum, String> properties);

    Boolean isEnabledAbsolutePath();

    String getTagsPrefix();

    String getPathSuffix();

    String getCategoriesPrefix();

    PostPermalinkType getPostPermalinkType();

    @Nullable
    <T extends Enum<T>> T getEnumByPropertyOrDefault(@NonNull PropertyEnum propertyEnum, @NonNull Class<T> valueType, @Nullable T defaultValue);

    @NonNull
    <T extends Enum<T>> Optional<T> getEnumByProperty(@NonNull PropertyEnum property, @NonNull Class<T> valueType);

    String getArchivesPrefix();

    @NonNull
    Object getByPropertyOfNonNull(@NonNull PropertyEnum property);

    @NonNull
    Object getByKeyOfNonNull(@NonNull String key);

    long getBirthday();

    @NonNull
    List<OptionDTO> listDtos();

    Page<OptionSimpleDTO> pageDtosBy(@NonNull Pageable pageable, OptionQuery optionQuery);

    @NonNull
    OptionSimpleDTO convertToDto(@NonNull Option option);


    void update(@NonNull Integer optionId, @NonNull OptionParam optionParam);

    @NonNull
    Option removePermanently(@NonNull Integer id);

    @NonNull
    Map<String, Object> listOptions(@Nullable List<String> keys);

    String getSheetPrefix();

    String getLinksPrefix();

    String getPhotosPrefix();

    String getJournalsPrefix();
}
