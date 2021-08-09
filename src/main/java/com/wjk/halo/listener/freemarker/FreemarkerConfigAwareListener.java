package com.wjk.halo.listener.freemarker;

import com.wjk.halo.event.user.UserUpdatedEvent;
import com.wjk.halo.service.UserService;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FreemarkerConfigAwareListener {

    private final Configuration configuration;
    private final UserService userService;

    public FreemarkerConfigAwareListener(Configuration configuration, UserService userService) {
        this.configuration = configuration;
        this.userService = userService;
    }

    @EventListener
    public void onUserUpdate(UserUpdatedEvent event) throws TemplateModelException{
        log.debug("Received user updated event, user id: [{}]", event.getUserId());

        loadUserConfig();
    }

    private void loadUserConfig() throws TemplateModelException{
        configuration.setSharedVariable("user", userService.getCurrentUser().orElse(null));
        log.debug("Loaded user");
    }

}
