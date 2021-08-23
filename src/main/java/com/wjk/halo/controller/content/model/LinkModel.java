package com.wjk.halo.controller.content.model;

import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.ThemeService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class LinkModel {
    private final ThemeService themeService;

    private final OptionService optionService;

    public LinkModel(ThemeService themeService,
                     OptionService optionService) {
        this.themeService = themeService;
        this.optionService = optionService;
    }

    public String list(Model model) {
        model.addAttribute("is_links", true);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("links");
    }
}
