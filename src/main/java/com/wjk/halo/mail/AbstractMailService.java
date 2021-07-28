package com.wjk.halo.mail;

import com.wjk.halo.exception.EmailException;
import com.wjk.halo.model.properties.EmailProperties;
import com.wjk.halo.service.OptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class AbstractMailService implements MailService {

    private static final int DEFAULT_POOL_SIZE = 5;
    protected final OptionService optionService;
    private JavaMailSender cachedMailSender;
    private MailProperties cachedMailProperties;
    private String cachedFromName;

    @Nullable
    private ExecutorService executorService;

    protected AbstractMailService(OptionService optionService) {
        this.optionService = optionService;
    }

    protected void clearCache(){
        this.cachedMailSender = null;
        this.cachedFromName = null;
        this.cachedMailProperties = null;
        log.debug("Cleared all mail caches");
    }

    @Override
    public void testConnection() {
        JavaMailSender javaMailSender = getMailSender();
        if (javaMailSender instanceof JavaMailSenderImpl){
            JavaMailSenderImpl mailSender = (JavaMailSenderImpl) javaMailSender;
            try {
                mailSender.testConnection();
            }catch (MessagingException e){
                throw new EmailException("无法连接到邮箱服务器，请检查邮箱配置.[" + e.getMessage() + "]", e);
            }
        }
    }

    protected interface Callback{
        void handle(@NonNull MimeMessageHelper messageHelper) throws Exception;
    }

    protected void sendMailTemplate(@Nullable Callback callback){
        if (callback == null){
            log.info("Callback is null, skip to send email");
            return;
        }
        Boolean emailEnabled = optionService.getByPropertyOrDefault(EmailProperties.ENABLED, Boolean.class);

        if (!emailEnabled){
            log.info("Email has been disabled by yourself, you can re-enable it through email settings on admin page.");
            return;
        }

        JavaMailSender mailSender = getMailSender();
        printMailConfig();

        MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage());

        try {
            messageHelper.setFrom(getFromAddress(mailSender));
            callback.handle(messageHelper);
            MimeMessage mimeMessage = messageHelper.getMimeMessage();
            mailSender.send(mimeMessage);
            log.info("Sent an email to [{}] successfully, subject: [{}], sent date: [{}]",
                    Arrays.toString(mimeMessage.getAllRecipients()),
                    mimeMessage.getSubject(),
                    mimeMessage.getSentDate());
        }catch (Exception e) {
            throw new EmailException("邮件发送失败，请检查 SMTP 服务配置是否正确", e);
        }

    }

    private synchronized InternetAddress getFromAddress(@NonNull JavaMailSender javaMailSender) throws UnsupportedEncodingException{
        if (StringUtils.isBlank(this.cachedFromName)){
            this.cachedFromName = optionService.getByPropertyOfNonNull(EmailProperties.FROM_NAME).toString();
        }
        if (javaMailSender instanceof JavaMailSenderImpl){
            JavaMailSenderImpl mailSender = (JavaMailSenderImpl) javaMailSender;
            String username = mailSender.getUsername();
            return new InternetAddress(username, this.cachedFromName, mailSender.getDefaultEncoding());
        }
        throw new UnsupportedOperationException("Unsupported java mail sender: " + javaMailSender.getClass().getName());
    }

    private void printMailConfig(){
        if (!log.isDebugEnabled()){
            return;
        }
        MailProperties mailProperties = getMailProperties();
        log.debug(mailProperties.toString());
    }

    @NonNull
    private synchronized JavaMailSender getMailSender(){
        if (this.cachedMailSender == null){
            MailSenderFactory mailSenderFactory = new MailSenderFactory();
            this.cachedMailSender = mailSenderFactory.getMailSender(getMailProperties());
        }
        return this.cachedMailSender;
    }

    @NonNull
    private synchronized MailProperties getMailProperties(){
        if (cachedMailProperties == null){
            MailProperties mailProperties = new MailProperties(log.isDebugEnabled());

            // set properties
            mailProperties.setHost(optionService.getByPropertyOrDefault(EmailProperties.HOST, String.class));
            mailProperties.setPort(optionService.getByPropertyOrDefault(EmailProperties.SSL_PORT, Integer.class));
            mailProperties.setUsername(optionService.getByPropertyOrDefault(EmailProperties.USERNAME, String.class));
            mailProperties.setPassword(optionService.getByPropertyOrDefault(EmailProperties.PASSWORD, String.class));
            mailProperties.setProtocol(optionService.getByPropertyOrDefault(EmailProperties.PROTOCOL, String.class));
            this.cachedMailProperties = mailProperties;
        }
        return this.cachedMailProperties;
    }

    protected void sendMailTemplate(boolean tryToAsync, @Nullable Callback callback){
        ExecutorService executorService = getExecutorService();
        if (tryToAsync && executorService != null){
            executorService.execute(() -> sendMailTemplate(callback));
        }
    }

    @NonNull
    public ExecutorService getExecutorService(){
        if (this.executorService == null){
            this.executorService = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
        }
        return executorService;
    }

}
