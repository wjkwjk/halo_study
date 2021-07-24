package com.wjk.halo.mail;

import java.util.HashMap;
import java.util.Map;

public class MailProperties extends org.springframework.boot.autoconfigure.mail.MailProperties {

    private Map<String, String> properties;

    public MailProperties(){
        this(false);
    }
    public MailProperties(boolean needDebug){
        addProperties("mail.debug", Boolean.toString(needDebug));
        addProperties("mail.smtp.auth", Boolean.TRUE.toString());
        addProperties("mail.smtp.ssl.enable", Boolean.TRUE.toString());
        addProperties("mail.smtp.timeout", "10000");
    }

    public void addProperties(String key, String value){
        if (properties == null){
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

}
