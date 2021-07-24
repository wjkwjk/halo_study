package com.wjk.halo.mail;

import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.CollectionUtils;

import java.util.Properties;

public class MailSenderFactory {

    @NonNull
    public JavaMailSender getMailSender(@NonNull MailProperties mailProperties){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        setProperties(mailSender, mailProperties);
        return mailSender;
    }

    private void setProperties(@NonNull JavaMailSenderImpl mailSender, @NonNull MailProperties mailProperties){
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());
        mailSender.setProtocol(mailProperties.getProtocol());
        mailSender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());

        if (!CollectionUtils.isEmpty(mailProperties.getProperties())) {
            Properties properties = new Properties();
            properties.putAll(mailProperties.getProperties());
            mailSender.setJavaMailProperties(properties);
        }
    }

}
