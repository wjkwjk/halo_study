package com.wjk.halo.config;

import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.event.StaticStorageChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;
import static com.wjk.halo.utils.HaloUtils.ensureBoth;

/**
 * 该类作用有两个，一个作用类似路径拦截，当请求路径符合blackPatterns时，不进行分发
 * 第二个作用用于监听StaticStorageChangedEvent事件， 用于监听StaticStorageChangedEvent事件，
 * 主要是用户上传了文件，或者重命名，有可能会创建新的目录，而这些目录也是不能通过请求地址直接访问的，因此需要重新生成路径匹配模式
 */

@Slf4j
public class HaloRequestMappingHandlerMapping extends RequestMappingHandlerMapping implements ApplicationListener<StaticStorageChangedEvent> {

    /**
     * 路径模式
     * 当接收到符合该路径模型的请求时，不进行分发，即不处理
     */
    private final Set<String> blackPatterns = new HashSet<>(16);

    /**
     * 路径匹配器，用于匹配路径是否属于某个路径模式
     */
    private final PathMatcher pathMatcher;

    private final HaloProperties haloProperties;

    public HaloRequestMappingHandlerMapping(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
        this.initBlackPatterns();
        /**
         * 使用Ant风格的路径匹配器
         *      ? : 匹配文件名中的一个字符
         *      * : 匹配文件中的任意多个字符
         *      ** : 匹配任意多层路径
         */
        pathMatcher = new AntPathMatcher();
    }

    /**
     * 初始化不进行处理的请求路径
     */
    private void initBlackPatterns() {
        //ensureBoth（a,b）表示往字符串a的两端都加上字符串b，若已经有了，则不加
        //因此该行结果为：/upload/**
        String uploadUrlPattern = ensureBoth(haloProperties.getUploadUrlPrefix(), URL_SEPARATOR) + "**";
        //结果为： /admin/?*/**
        String adminPathPattern = ensureBoth(haloProperties.getAdminPath(), URL_SEPARATOR) + "?*/**";

        blackPatterns.add("/themes/**");
        blackPatterns.add("/js/**");
        blackPatterns.add("/images/**");
        blackPatterns.add("/fonts/**");
        blackPatterns.add("/css/**");
        blackPatterns.add("/assets/**");
        blackPatterns.add("/color.less");
        blackPatterns.add("/swagger-ui.html");
        blackPatterns.add("/csrf");
        blackPatterns.add("/webjars/**");
        blackPatterns.add(uploadUrlPattern);
        blackPatterns.add(adminPathPattern);
    }

    /**
     * 路径匹配器PathMatcher的match方法
     *      boolean match(String pattern, String path);
     *      根据当前 PathMatcher 的匹配策略，检查指定的径 path 和指定的模式 pattern 是否匹配，
     *      pattern:用于检测路径字符串是否匹配于某个模式时所用的模式
     * 	    path:需要被检测的路径字符串
     * 	    返回true 表示匹配， false 表示不匹配
     */

    @Override
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        log.debug("Looking path: [{}]", lookupPath);
        for (String blackPattern : blackPatterns){
            if (this.pathMatcher.match(blackPattern, lookupPath)){
                log.debug("Skipped path [{}] with pattern: [{}]", lookupPath, blackPattern);
                return null;
            }
        }
        //再调用父类方法，将该请求分发到合适的接口（controller）处理
        return super.lookupHandlerMethod(lookupPath, request);
    }

    /**
     * 用于监听StaticStorageChangedEvent事件，主要是用户上传了文件，或者重命名，有可能会创建新的目录，而这些目录也是不能
     * 通过请求地址直接访问的，因此需要重新生成路径匹配模式
     * @param event
     */

    @Override
    public void onApplicationEvent(StaticStorageChangedEvent event) {
        //该目录就是用来存储用户上传的文件
        Path staticPath = event.getStaticPath();
        try (Stream<Path> rootPathStream = Files.list(staticPath)) {
            synchronized (this){
                /**
                 * 重新生成路径匹配模式，因为用户上传文件后，可能会生成新的文件夹，这些文件夹也是用于不能直接通过地址访问的
                 * 因此需要重新生成路径匹配模式，将这些新的文件夹加入进来
                 *
                 * 又因为有可能会同时出现多个事件，因此需要加锁
                 */
                blackPatterns.clear();
                initBlackPatterns();
                rootPathStream.forEach(rootPath -> {
                    if (Files.isDirectory(rootPath)){
                        String directoryPattern = "/" + rootPath.getFileName().toString() + "/**";
                        blackPatterns.add(directoryPattern);
                        log.debug("Exclude for folder path pattern: [{}]", directoryPattern);
                    }else {
                        String pathPattern = "/" + rootPath.getFileName().toString();
                        blackPatterns.add(pathPattern);
                        log.debug("Exclude for file path pattern: [{}]", pathPattern);
                    }
                });
            }
        }catch (IOException e){
            log.error("Failed to refresh static directory mapping", e);
        }
    }
}
