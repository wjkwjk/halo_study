package com.wjk.halo.controller.admin.api;

import cn.hutool.crypto.SecureUtil;
import com.wjk.halo.cache.lock.CacheLock;
import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.entity.PostComment;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.params.*;
import com.wjk.halo.model.properties.BlogProperties;
import com.wjk.halo.model.properties.OtherProperties;
import com.wjk.halo.model.properties.PrimaryProperties;
import com.wjk.halo.model.properties.PropertyEnum;
import com.wjk.halo.model.support.BaseResponse;
import com.wjk.halo.model.support.CreateCheck;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.service.*;
import com.wjk.halo.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/api/admin/installations")
public class InstallController {


    private final UserService userService;
    private final CategoryService categoryService;
    private final PostService postService;
    private final SheetService sheetService;
    private final PostCommentService postCommentService;
    private final OptionService optionService;
    private final MenuService menuService;
    private final ApplicationEventPublisher eventPublisher;

    public InstallController(UserService userService, CategoryService categoryService, PostService postService, SheetService sheetService, PostCommentService postCommentService, OptionService optionService, MenuService menuService, ApplicationEventPublisher eventPublisher) {
        this.optionService = optionService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.postService = postService;
        this.sheetService = sheetService;
        this.postCommentService = postCommentService;
        this.menuService = menuService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    @ResponseBody
    @CacheLock
    public BaseResponse<String> installBlog(@RequestBody InstallParam installParam){

        //????????????????????????
        ValidationUtils.validate(installParam, CreateCheck.class);

        //?????????????????????
        boolean isInstalled = optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);
        if (isInstalled){
            throw new BadRequestException("?????????????????????????????????????????????");
        }

        initSettings(installParam);

        User user = createUser(installParam);

        Category category = createDefaultCategoryIfAbsent();

        PostDetailVO post = createDefaultPostIfAbsent(category);

        createDefaultSheet();

        createDefaultComment(post);

        createDefaultMenu();

        eventPublisher.publishEvent(new LogEvent(this, user.getId().toString(), LogType.BLOG_INITIALIZED, "????????????????????????"));

        return BaseResponse.ok("????????????! ");


    }

    //???????????????
    private void initSettings(InstallParam installParam){
        //?????????????????????
        Map<PropertyEnum, String> properties = new HashMap<>(11);
        properties.put(PrimaryProperties.IS_INSTALLED, Boolean.TRUE.toString());
        properties.put(BlogProperties.BLOG_LOCATE, installParam.getLocate());
        properties.put(BlogProperties.BLOG_TITLE, installParam.getTitle());
        properties.put(BlogProperties.BLOG_URL, StringUtils.isBlank(installParam.getUrl()) ? optionService.getBlogBaseUrl() : installParam.getUrl());
        //????????????????????????????????????
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
            userService.setPassword(user, installParam.getPassword());//???????????????
            return userService.update(user);
        }).orElseGet(() -> {
            //??????????????????????????????email???gravator?????????????????????
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
        category.setName("????????????");
        category.setSlug("default");
        category.setDescription("??????????????????????????????????????????????????????");
        ValidationUtils.validate(category);
        return categoryService.create(category.convertTo());//category.convertTo():???CategoryParam?????????Category
    }

    //?????????????????????????????????????????????????????????
    @Nullable
    private PostDetailVO createDefaultPostIfAbsent(@Nullable Category category){
        long publishedCount = postService.countByStatus(PostStatus.PUBLISHED);

        if (publishedCount > 0){
            return null;
        }

        PostParam postParam = new PostParam();
        postParam.setSlug("hello-halo");
        postParam.setTitle("Hello halo");
        postParam.setStatus(PostStatus.PUBLISHED);
        postParam.setOriginalContent("????????????????????????");

        Set<Integer> categoryIds = new HashSet<>();
        if (category != null){
            categoryIds.add(category.getId());
            postParam.setCategoryIds(categoryIds);
        }
        return postService.createBy(postParam.convertTo(), Collections.emptySet(), categoryIds, false);
    }

    @Nullable
    private void createDefaultSheet(){
        long publishedCount = sheetService.countByStatus(PostStatus.PUBLISHED);
        if (publishedCount > 0){
            return;
        }

        SheetParam sheetParam = new SheetParam();
        sheetParam.setSlug("about");
        sheetParam.setTitle("????????????");
        sheetParam.setStatus(PostStatus.PUBLISHED);
        sheetParam.setOriginalContent("## ????????????\n" +
                "\n" +
                "??????????????????????????????????????????????????? `??????` -> `????????????` -> `???????????????` ??????????????????????????????????????????????????????????????????????????????????????????????????????\n" +
                "\n" +
                "> ??????????????????????????????????????????????????????????????????");
        sheetService.createBy(sheetParam.convertTo(), false);
    }

    @Nullable
    private void createDefaultComment(@Nullable PostDetailVO post){
        if (post == null){
            return;
        }
        long commentCount = postCommentService.count();

        if (commentCount > 0){
            return;
        }

        PostComment comment = new PostComment();
        comment.setAuthor("Halo");
        comment.setAuthorUrl("https://halo.run");
        comment.setContent("???????????? Halo????????????????????????????????????????????? [Gravatar](https://cn.gravatar.com)??????????????????????????? [Gravatar](https://cn.gravatar.com) ???????????????????????????");
        comment.setEmail("hi@halo.run");
        comment.setPostId(post.getId());
        postCommentService.create(comment);
    }

    private void createDefaultMenu(){
        long menuCount = menuService.count();

        if (menuCount > 0){
            return;
        }

        MenuParam menuIndex = new MenuParam();

        menuIndex.setName("??????");
        menuIndex.setUrl("/");
        menuIndex.setPriority(1);
        //???????????????
        menuService.create(menuIndex.convertTo());

        MenuParam menuArchive = new MenuParam();

        menuArchive.setName("????????????");
        menuArchive.setUrl("/archives");
        menuArchive.setPriority(2);
        menuService.create(menuArchive.convertTo());

        MenuParam menuCategory = new MenuParam();
        menuCategory.setName("????????????");
        menuCategory.setUrl("/categories/default");
        menuCategory.setPriority(3);
        menuService.create(menuCategory.convertTo());

        MenuParam menuSheet = new MenuParam();
        menuSheet.setName("????????????");
        menuSheet.setUrl("/s/about");
        menuSheet.setPriority(4);
        menuService.create(menuSheet.convertTo());
    }


}
