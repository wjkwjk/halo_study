package com.wjk.halo.service.impl;

import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.handler.theme.config.support.Group;
import com.wjk.halo.handler.theme.config.support.Item;
import com.wjk.halo.model.entity.ThemeSetting;
import com.wjk.halo.repository.ThemeSettingRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.ThemeService;
import com.wjk.halo.service.ThemeSettingService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.ServiceUtils;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Service
public class ThemeSettingServiceImpl extends AbstractCrudService<ThemeSetting, Integer> implements ThemeSettingService {

    private final ThemeSettingRepository themeSettingRepository;

    private final ThemeService themeService;

    private final Configuration configuration;

    public ThemeSettingServiceImpl(ThemeSettingRepository themeSettingRepository,
                                   ThemeService themeService,
                                   Configuration configuration) {
        super(themeSettingRepository);
        this.themeSettingRepository = themeSettingRepository;
        this.themeService = themeService;
        this.configuration = configuration;
    }

    @Override
    public Map<String, Object> listAsMapBy(String themeId) {
        Map<String, Item> itemMap = getConfigItemMap(themeId);

        List<ThemeSetting> themeSettings = listBy(themeId);

        Map<String, Object> result = new HashMap<>();

        themeSettings.forEach(themeSetting -> {
            String key = themeSetting.getKey();

            Item item = itemMap.get(key);

            if (item == null){
                return;
            }
            Object convertedValue = item.getDataType().convertTo(themeSetting.getValue());
            log.debug("Converted user-defined data from [{}] to [{}], type: [{}]", themeSetting.getValue(), convertedValue, item.getDataType());

            result.put(key, convertedValue);
        });

        itemMap.forEach((name, item) -> {
            log.debug("Name: [{}], item: [{}]", name, item);

            if (item.getDefaultValue() == null || result.containsKey(name)){
                return;
            }

            Object convertedDefaultValue = item.getDataType().convertTo(item.getDefaultValue());
            log.debug("Converted pre-defined data from [{}] to [{}], type: [{}]", item.getDefaultValue(), convertedDefaultValue, item.getDataType());

            result.put(name, convertedDefaultValue);

        });
        return result;

    }

    @Override
    public List<ThemeSetting> listBy(String themeId) {

        return themeSettingRepository.findAllByThemeId(themeId);

    }

    @Override
    public ThemeSetting save(String key, String value, String themeId) {
        log.debug("Starting saving theme setting key: [{}], value: [{}]", key, value);

        // Find setting by key
        Optional<ThemeSetting> themeSettingOptional = themeSettingRepository.findByThemeIdAndKey(themeId, key);

        if (StringUtils.isBlank(value)) {
            // Delete it
            return themeSettingOptional
                    .map(setting -> {
                        themeSettingRepository.delete(setting);
                        log.debug("Removed theme setting: [{}]", setting);
                        return setting;
                    }).orElse(null);
        }

        // Get config item map
        Map<String, Item> itemMap = getConfigItemMap(themeId);

        // Get item info
        Item item = itemMap.get(key);

        // Update or create
        ThemeSetting themeSetting = themeSettingOptional
                .map(setting -> {
                    log.debug("Updating theme setting: [{}]", setting);
                    setting.setValue(value);
                    log.debug("Updated theme setting: [{}]", setting);
                    return setting;
                }).orElseGet(() -> {
                    ThemeSetting setting = new ThemeSetting();
                    setting.setKey(key);
                    setting.setValue(value);
                    setting.setThemeId(themeId);
                    log.debug("Creating theme setting: [{}]", setting);
                    return setting;
                });
        // Determine whether the data already exists
        if (themeSettingRepository.findOne(Example.of(themeSetting)).isPresent()) {
            return null;
        }
        // Save the theme setting
        return themeSettingRepository.save(themeSetting);
    }

    @Override
    public void save(Map<String, Object> settings, String themeId) {
        if (CollectionUtils.isEmpty(settings)) {
            return;
        }

        // Save the settings
        settings.forEach((key, value) -> save(key, value.toString(), themeId));

        try {
            configuration.setSharedVariable("settings", listAsMapBy(themeService.getActivatedThemeId()));
        } catch (TemplateModelException e) {
            throw new ServiceException("主题设置保存失败", e);
        }
    }

    private Map<String, Item> getConfigItemMap(@NonNull String themeId){
        List<Group> groups = themeService.fetchConfig(themeId);

        Set<Item> items = new LinkedHashSet<>();
        groups.forEach(group -> items.addAll(group.getItems()));

        return ServiceUtils.convertToMap(items, Item::getName);
    }

}
