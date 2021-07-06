package com.wjk.halo.controller.admin.api;

import cn.hutool.crypto.SecureUtil;
import com.wjk.halo.cache.lock.CacheLock;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.params.CategoryParam;
import com.wjk.halo.model.params.InstallParam;
import com.wjk.halo.model.properties.BlogProperties;
import com.wjk.halo.model.properties.OtherProperties;
import com.wjk.halo.model.properties.PrimaryProperties;
import com.wjk.halo.model.properties.PropertyEnum;
import com.wjk.halo.model.support.BaseResponse;
import com.wjk.halo.model.support.CreateCheck;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.service.CategoryService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostService;
import com.wjk.halo.service.UserService;
import com.wjk.halo.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/api/installations")
public class InstallController {


    private final OptionService optionService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final PostService postService;

    public InstallController(OptionService optionService, UserService userService, CategoryService categoryService, PostService postService) {
        this.optionService = optionService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.postService = postService;
    }

    @PostMapping
    @ResponseBody
    @CacheLock
    public BaseResponse<String>  installBlog(@RequestBody InstallParam installParam){

        ValidationUtils.validate(installParam, CreateCheck.class);

        boolean isInstalled = optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);

        if (isInstalled){
            throw new BadRequestException("该博客已被初始化，不能再次安装");
        }
        initSettings(installParam);

        User user = createUser(installParam);

        Category category = createDefaultCategoryIfAbsent();

        PostDetailVO post =


    }


    private void initSettings(InstallParam installParam){
        Map<PropertyEnum, String> properties = new HashMap<>(11);
        properties.put(PrimaryProperties.IS_INSTALLED, Boolean.TRUE.toString());
        properties.put(BlogProperties.BLOG_LOCATE, installParam.getLocate());
        properties.put(BlogProperties.BLOG_TITLE, installParam.getTitle());
        properties.put(BlogProperties.BLOG_URL, StringUtils.isBlank(installParam.getUrl()) ? optionService.getBlogBaseUrl() : installParam.getUrl());

        Long birthday = optionService.getByPropertyOrDefault(PrimaryProperties.BIRTHDAY, Long.class, 0L);

        if (birthday.equals(0L)){
            properties.put(PrimaryProperties.BIRTHDAY, String.valueOf(System.currentTimeMillis()));
        }

        Boolean globalAbsolutePathEnabled = optionService.getByPropertyOrDefault(OtherProperties.GLOBAL_ABSOLUTE_PATH_ENABLED, Boolean.class, null);

        if (globalAbsolutePathEnabled == null){
            properties.put(OtherProperties.GLOBAL_ABSOLUTE_PATH_ENABLED, Boolean.FALSE.toString());
        }

        optionService.saveProperties(properties);
    }

    private User createUser(InstallParam installParam){
        return userService.getCurrentUser().map(user -> {
            installParam.update(user);
            userService.setPassword(user, installParam.getPassword());
            return userService.update(user);
        }).orElseGet(() -> {
            String gravator = "//cn.gravator.com/avator/" + SecureUtil.md5(installParam.getEmail())  + "?s=256&d=mm";
            installParam.setAvatar(gravator);
            return userService.createBy(installParam);
        });
    }

    @Nullable
    private Category createDefaultCategoryIfAbsent(){
        long categoryCount = categoryService.count();
        if (categoryCount > 0){
            return null;
        }
        CategoryParam category = new CategoryParam();
        category.setName("默认分类");
        category.setSlug("default");
        category.setDescription("这是你的默认分类，如不需要，删除即可");
        ValidationUtils.validate(category);
        return categoryService.create(category.convertTo());
    }

    @Nullable
    private PostDetailVO createDefaultPostIfAbsent(@Nullable Category category){
        long publishedCount = postService.coun
    }

}
