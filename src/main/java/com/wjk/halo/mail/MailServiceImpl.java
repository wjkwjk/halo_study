package com.wjk.halo.mail;

import com.wjk.halo.event.options.OptionUpdateEvent;
import com.wjk.halo.service.OptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Slf4j
@Service
public class MailServiceImpl extends AbstractMailService implements ApplicationListener<OptionUpdateEvent> {

    private final FreeMarkerConfigurer freeMarker;

    public MailServiceImpl(FreeMarkerConfigurer freeMarker, OptionService optionService) {
        super(optionService);
        this.freeMarker = freeMarker;
    }

    @Override
    public void onApplicationEvent(OptionUpdateEvent event) {
        clearCache();
    }

    @Override
    public void sendTextMail(String to, String subject, String content) {
        sendMailTemplate(true, messageHelper -> {
            messageHelper.setSubject(subject);
            messageHelper.setTo(to);
            messageHelper.setText(content);
        });
    }

    @Override
    public void testConnection() {
        super.testConnection();
    }
}
