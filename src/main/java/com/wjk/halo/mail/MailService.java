package com.wjk.halo.mail;

import java.util.Map;

public interface MailService {

    void sendTextMail(String to, String subject, String content);

    void testConnection();

    void sendTemplateMail(String to, String subject, Map<String, Object> content, String templateName);
}
