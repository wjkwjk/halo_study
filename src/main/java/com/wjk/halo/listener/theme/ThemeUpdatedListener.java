package com.wjk.halo.listener.theme;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.event.options.OptionUpdatedEvent;
import com.wjk.halo.event.theme.ThemeUpdatedEvent;
import com.wjk.halo.service.ThemeService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ThemeUpdatedListener {

    private final AbstractStringCacheStore cacheStore;

    public ThemeUpdatedListener(AbstractStringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @EventListener
    public void onApplicationEvent(ThemeUpdatedEvent event) {
        cacheStore.delete(ThemeService.THEMES_CACHE_KEY);
    }

    @EventListener
    public void onOptionUpdatedEvent(OptionUpdatedEvent optionUpdatedEvent) {
        cacheStore.delete(ThemeService.THEMES_CACHE_KEY);
    }

}
