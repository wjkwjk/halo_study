package com.wjk.halo.mail;

public interface MailService {

    void sendTextMail(String to, String subject, String content);

    void testConnection();
}
