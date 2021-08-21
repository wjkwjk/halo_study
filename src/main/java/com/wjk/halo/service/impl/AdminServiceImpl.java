package com.wjk.halo.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.mail.MailService;
import com.wjk.halo.model.dto.EnvironmentDTO;
import com.wjk.halo.model.dto.StatisticDTO;
import com.wjk.halo.model.enums.CommentStatus;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.properties.EmailProperties;
import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.enums.MFAType;
import com.wjk.halo.model.params.LoginParam;
import com.wjk.halo.model.params.ResetPasswordParam;
import com.wjk.halo.model.support.HaloConst;
import com.wjk.halo.security.authentication.Authentication;
import com.wjk.halo.security.context.SecurityContextHolder;
import com.wjk.halo.security.token.AuthToken;
import com.wjk.halo.security.util.SecurityUtils;
import com.wjk.halo.service.*;
import com.wjk.halo.utils.FileUtils;
import com.wjk.halo.utils.HaloUtils;
import com.wjk.halo.utils.TwoFactorAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.wjk.halo.model.support.HaloConst.*;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final PostService postService;
    private final SheetService sheetService;
    private final AttachmentService attachmentService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final AbstractStringCacheStore cacheStore;
    private final OptionService optionService;
    private final MailService mailService;
    private final PostCommentService postCommentService;
    private final SheetCommentService sheetCommentService;
    private final JournalCommentService journalCommentService;
    private final LinkService linkService;
    private final HaloProperties haloProperties;
    private final RestTemplate restTemplate;

    public AdminServiceImpl(PostService postService,
                            SheetService sheetService,
                            AttachmentService attachmentService,
                            PostCommentService postCommentService,
                            SheetCommentService sheetCommentService,
                            JournalCommentService journalCommentService,
                            OptionService optionService,
                            UserService userService,
                            LinkService linkService,
                            MailService mailService,
                            AbstractStringCacheStore cacheStore,
                            RestTemplate restTemplate,
                            HaloProperties haloProperties,
                            ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.cacheStore = cacheStore;
        this.optionService = optionService;
        this.mailService = mailService;
        this.postService = postService;
        this.sheetService = sheetService;
        this.attachmentService = attachmentService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.journalCommentService = journalCommentService;
        this.linkService = linkService;
        this.haloProperties = haloProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * 整个流程为
     *  先根据用户名或者邮箱查找数据库，然后判断数据库中是否有对应的用户，若无报错，若有
     *  判断身份是否已被停用
     *  再判断密码是否匹配
     *
     * @param loginParam
     * @return
     */
    @Override
    @NonNull
    public User authenticate(@NonNull LoginParam loginParam) {
        String username = loginParam.getUsername();

        String mismatchTip = "用户名或者密码不正确";

        final User user;

        try {
            user = Validator.isEmail(username) ?
                    userService.getByEmailOfNonNull(username) :
                    userService.getByUsernameOfNonNull(username);
        }catch (NotFoundException e){
            log.error("Failed to find user by name: " + username);
            eventPublisher.publishEvent(new LogEvent(this, loginParam.getUsername(), LogType.LOGIN_FAILED, loginParam.getUsername()));
            throw new BadRequestException(mismatchTip);
        }

        userService.mustNotExpire(user);

        if (!userService.passwordMatch(user, loginParam.getPassword())){
            eventPublisher.publishEvent(new LogEvent(this, loginParam.getUsername(), LogType.LOGIN_FAILED, loginParam.getUsername()));

            throw new BadRequestException(mismatchTip);

        }
        return user;

    }

    @Override
    @NonNull
    public AuthToken authCodeCheck(@NonNull final LoginParam loginParam) {
        //相当于precheck做的检查
        final User user = this.authenticate(loginParam);

        //判断是否使用验证码登陆
        if (MFAType.useMFA(user.getMfaType())){
            //两步验证码判断
            if (StrUtil.isBlank(loginParam.getAuthcode())){
                throw new BadRequestException("请输入两步验证码");
            }
            TwoFactorAuthUtils.validateTFACode(user.getMfaKey(), loginParam.getAuthcode());
        }

        if (SecurityContextHolder.getContext().isAuthenticated()){
            throw new BadRequestException("您已登陆，请不要重复登陆");
        }

        eventPublisher.publishEvent(new LogEvent(this, user.getUsername(), LogType.LOGIN_IN, user.getNickname()));
        //生成token
        return buildAuthToken(user);
    }

    @Override
    public void clearToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null){
            throw new BadRequestException("您尚未登陆, 因此无法注销");
        }

        User user = authentication.getDetail().getUser();

        cacheStore.getAny(SecurityUtils.buildAccessTokenKey(user), String.class).ifPresent(accessToken -> {
            cacheStore.delete(SecurityUtils.buildTokenAccessKey(accessToken));
            cacheStore.delete(SecurityUtils.buildAccessTokenKey(user));
        });

        cacheStore.getAny(SecurityUtils.buildRefreshTokenKey(user), String.class).ifPresent(refreshToken -> {
            cacheStore.delete(SecurityUtils.buildTokenRefreshKey(refreshToken));
            cacheStore.delete(SecurityUtils.buildRefreshTokenKey(user));
        });

        eventPublisher.publishEvent(new LogEvent(this, user.getUsername(), LogType.LOGGED_OUT, user.getNickname()));

        log.info("You have been logged out, looking forward to your next visit!");

    }

    @Override
    public void sendResetPasswordCode(ResetPasswordParam param) {
        cacheStore.getAny("code", String.class).ifPresent(code -> {
            throw new ServiceException("已经获取过验证码，不能重复获取");
        });

        if (!userService.verifyUser(param.getUsername(), param.getEmail())){
            throw new ServiceException("用户名或者邮箱验证错误");
        }

        String code = RandomUtil.randomNumbers(6);

        log.info("Got reset password code:{}", code);

        cacheStore.putAny("code", code, 5, TimeUnit.MINUTES);

        Boolean emailEnabled = optionService.getByPropertyOrDefault(EmailProperties.ENABLED, Boolean.class, false);

        if (!emailEnabled){
            throw new ServiceException("未启用 SMTP 服务，无法发送邮件，但是你可以通过系统日志找到验证码");
        }

        String content = "您正在进行密码重置操作，如不是本人操作，请尽快做好相应措施。密码重置验证码如下（五分钟有效）：\n" + code;
        mailService.sendTextMail(param.getEmail(), "找回密码验证码", content);

    }

    @Override
    public void resetPasswordByCode(ResetPasswordParam param) {
        if (StringUtils.isEmpty(param.getCode())){
            throw new ServiceException("验证码不能为空");
        }

        if (StringUtils.isEmpty(param.getPassword())){
            throw new ServiceException("密码不能为空");
        }

        if (!userService.verifyUser(param.getUsername(), param.getEmail())){
            throw new ServiceException("用户名或者邮箱验证错误");
        }

        String code = cacheStore.getAny("code", String.class).orElseThrow(()->new ServiceException("未获取过验证码"));
        if (!code.equals(param.getCode())){
            throw new ServiceException("验证码不正确");
        }

        User user = userService.getCurrentUser().orElseThrow(() -> new ServiceException("未查询到博主信息"));

        userService.setPassword(user, param.getPassword());

        userService.update(user);

        cacheStore.delete("code");
    }

    @Override
    @NonNull
    public AuthToken refreshToken(String refreshToken) {
        Integer userId = cacheStore.getAny(SecurityUtils.buildTokenRefreshKey(refreshToken), Integer.class)
                .orElseThrow(() -> new BadRequestException("登录状态已失效，请重新登录").setErrorData(refreshToken));

        User user = userService.getById(userId);

        cacheStore.getAny(SecurityUtils.buildAccessTokenKey(user), String.class)
                .ifPresent(accessToken -> cacheStore.delete(SecurityUtils.buildTokenAccessKey(accessToken)));
        cacheStore.delete(SecurityUtils.buildTokenRefreshKey(refreshToken));
        cacheStore.delete(SecurityUtils.buildAccessTokenKey(user));
        cacheStore.delete(SecurityUtils.buildRefreshTokenKey(user));

        return buildAuthToken(user);
    }

    @Override
    @NonNull
    public StatisticDTO getCount() {
        StatisticDTO statisticDTO = new StatisticDTO();
        statisticDTO.setPostCount(postService.countByStatus(PostStatus.PUBLISHED) + sheetService.countByStatus(PostStatus.PUBLISHED));
        statisticDTO.setAttachmentCount(attachmentService.count());

        long postCommentCount = postCommentService.countByStatus(CommentStatus.PUBLISHED);
        long sheetCommentCount = sheetCommentService.countByStatus(CommentStatus.PUBLISHED);
        long journalCommentCount = journalCommentService.countByStatus(CommentStatus.PUBLISHED);

        statisticDTO.setCommentCount(postCommentCount + sheetCommentCount + journalCommentCount);

        long birthday = optionService.getBirthday();
        long days = (System.currentTimeMillis() - birthday) / (1000 * 24 * 3600);
        statisticDTO.setEstablishDays(days);
        statisticDTO.setBirthday(birthday);

        statisticDTO.setLinkCount(linkService.count());

        statisticDTO.setVisitCount(postService.countVisit() + sheetService.countVisit());
        statisticDTO.setLikeCount(postService.countLike() + sheetService.countLike());

        return statisticDTO;
    }

    @Override
    @NonNull
    public EnvironmentDTO getEnvironments() {
        EnvironmentDTO environmentDTO = new EnvironmentDTO();

        environmentDTO.setStartTime(ManagementFactory.getRuntimeMXBean().getStartTime());
        environmentDTO.setDatabase(DATABASE_PRODUCT_NAME);
        environmentDTO.setVersion(HALO_VERSION);
        environmentDTO.setMode(haloProperties.getMode());
        return environmentDTO;

    }

    /**
     * 更新系统代码
     * 首先从githup获取最新版本的代码，然后删除原本的备份代码，将当前使用的系统代码复制到备份目录，
     * 然后判断最新获取的代码是否可用，可用的话，再复制到正在使用的目录
     */
    @Override
    public void updateAdminAssets() {
        //用于向githup仓库请求最新的版本信息，还未获取具体的代码
        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(HALO_ADMIN_RELEASES_LATEST, Map.class);

        if (responseEntity.getStatusCode().isError() || responseEntity.getBody() == null){
            log.debug("Failed to request remote url: [{}]", HALO_ADMIN_RELEASES_LATEST);
            throw new ServiceException("系统无法访问到 Github 的 API").setErrorData(HALO_ADMIN_RELEASES_LATEST);
        }
        //获取版本信息
        Object assetsObject = responseEntity.getBody().get("assets");

        if (!(assetsObject instanceof List)){
            throw new ServiceException("Github API 返回内容有误").setErrorData(assetsObject);
        }

        try {
            List<?> assets = (List<?>) assetsObject;
            Map assetMap = (Map) assets.stream()
                    .filter(assetPredicate())   //过滤掉没有满足名字和格式的版本
                    .findFirst()    //获取满足条件中的最新的系统版本
                    .orElseThrow(() -> new ServiceException("Halo admin 最新版暂无资源文件，请稍后再试"));

            //获得下载地址
            Object browserDownloadUrl = assetMap.getOrDefault("browser_download_url", "");

            //向browserDownloadUrl地址发出一个get请求，并用byte数组接收返回内容，此时请求的是源代码
            ResponseEntity<byte[]> downloadResponseEntity = restTemplate.getForEntity(browserDownloadUrl.toString(), byte[].class);

            if (downloadResponseEntity.getStatusCode().isError() || downloadResponseEntity.getBody() == null){
                throw new ServiceException("Failed to request remote url: " + browserDownloadUrl.toString()).setErrorData(browserDownloadUrl.toString());
            }

            //返回：/用户根目录/.halo/templates/admin/
            String adminTargetName = haloProperties.getWorkDir() + HALO_ADMIN_RELATIVE_PATH;

            /**
             * 使用adminTargetName构造一个Path对象
             * Path就是对目录的一个封装，方便获取目录的各个信息，例如是否是绝对路径等
             */
            //当时正在使用的系统代码的目录
            Path adminPath = Paths.get(adminTargetName);

            //备份目录
            //返回：/用户根目录/.halo/templates/admin-backup/
            Path adminBackupPath = Paths.get(haloProperties.getWorkDir(), HALO_ADMIN_RELATIVE_BACKUP_PATH);

            /**
             * 将当前使用的系统代码作为备份，拷贝到备份目录下
             */
            backupAndClearAdminAssetsIfPresent(adminPath, adminBackupPath);

            /**
             * 生成Path，用来存储最新获取的系统代码
             * 代码放在一个临时目录中
             */
            Path assetTempPath = FileUtils.createTempDirectory()
                    .resolve(assetMap.getOrDefault("name", "halo-admin-latest.zip").toString());

            //将最新获取的系统代码解压到临时目录
            FileUtils.unzip(downloadResponseEntity.getBody(), assetTempPath);

            Path adminRootPath = FileUtils.findRootPath(assetTempPath,
                    path -> StringUtils.equalsIgnoreCase("index.html", path.getFileName().toString()))
                    .orElseThrow(() -> new BadRequestException("无法准确定位到压缩包的根路径，请确认包含 index.html 文件。"));

            /**
             * 将最新获取的系统代码拷贝到当前使用的目录下
             */
            FileUtils.copyFolder(adminRootPath, adminPath);
        }catch (Throwable t){
            throw new ServiceException("更新 Halo admin 失败，" + t.getMessage(), t);
        }
    }

    @Override
    public String getLogFiles(Long lines) {
        File file = new File(haloProperties.getWorkDir(), LOG_PATH);
        List<String> linesArray = new ArrayList<>();
        StringBuilder result = new StringBuilder();

        if (!file.exists()){
            return StringUtils.EMPTY;
        }
        long count = 0;

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long length = randomAccessFile.length();
            if (length == 0L){
                return StringUtils.EMPTY;
            }else {
                long pos = length - 1;
                while (pos > 0){
                    pos--;
                    randomAccessFile.seek(pos);
                    if (randomAccessFile.readByte() == '\n'){
                        String line = randomAccessFile.readLine();
                        linesArray.add(new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                        count++;
                        if (count == lines){
                            break;
                        }
                    }
                }
                if (pos == 0){
                    randomAccessFile.seek(0);
                    linesArray.add(new String(randomAccessFile.readLine().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                }
            }
        }catch (Exception e){
            throw new ServiceException("读取日志失败", e);
        }finally {
            if (randomAccessFile != null){
                try{
                    randomAccessFile.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        Collections.reverse(linesArray);

        linesArray.forEach(line -> {
            result.append(line)
                    .append(StringUtils.LF);
        });
        return result.toString();
    }

    /**
     * 用来判断获取的系统版本以及获取的格式是否满足条件
     * @return
     */

    @NonNull
    private Predicate<Object> assetPredicate(){
        return asset -> {
            if (!(asset instanceof Map)){
                return false;
            }
            Map aAssetMap = (Map) asset;
            String contentType = aAssetMap.getOrDefault("content_type", "").toString();

            Object name = aAssetMap.getOrDefault("name", "");
            /**
             * 要求获取的系统版本的名称要满足HALO_ADMIN_VERSION_REGEX模式
             * 获取的格式为application/zip格式
             */
            //equalsIgnoreCase：字符串比较，忽略大小写
            return name.toString().matches(HALO_ADMIN_VERSION_REGEX) && "application/zip".equalsIgnoreCase(contentType);
        };
    }

    /**
     * 将当前使用的代码作为备份代码，拷贝到备份文件中
     * @param sourcePath
     * @param backupPath
     * @throws IOException
     */
    private void backupAndClearAdminAssetsIfPresent(@NonNull Path sourcePath, @NonNull Path backupPath) throws IOException{
        if (!FileUtils.isEmpty(sourcePath)){

            //备份目录
            Path adminPathBackup = Paths.get(haloProperties.getWorkDir(), HALO_ADMIN_RELATIVE_BACKUP_PATH);

            /**
             * 删除备份文件
             */
            FileUtils.deleteFolder(backupPath);

            /**
             * 将当前使用的系统代码复制到备份目录下
             */
            FileUtils.copyFolder(sourcePath, backupPath);

            /**
             * 删除当前使用的系统代码
             */
            FileUtils.deleteFolder(sourcePath);
        }else {
            FileUtils.createIfAbsent(sourcePath);
        }
    }

    @NonNull
    private AuthToken buildAuthToken(@NonNull User user){
        AuthToken token = new AuthToken();

        token.setAccessToken(HaloUtils.randomUUIDWithoutDash());
        token.setExpiredIn(ACCESS_TOKEN_EXPIRED_SECONDS);
        token.setRefreshToken(HaloUtils.randomUUIDWithoutDash());
        //将token信息放入缓存，其中key为指定前缀+用户id
        cacheStore.putAny(SecurityUtils.buildAccessTokenKey(user), token.getAccessToken(), ACCESS_TOKEN_EXPIRED_SECONDS, TimeUnit.SECONDS);
        cacheStore.putAny(SecurityUtils.buildRefreshTokenKey(user), token.getRefreshToken(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);
        cacheStore.putAny(SecurityUtils.buildTokenAccessKey(token.getAccessToken()), user.getId(), ACCESS_TOKEN_EXPIRED_SECONDS, TimeUnit.SECONDS);
        cacheStore.putAny(SecurityUtils.buildTokenRefreshKey(token.getRefreshToken()),user.getId(), REFRESH_TOKEN_EXPIRED_DAYS, TimeUnit.DAYS);

        return token;

    }

}
