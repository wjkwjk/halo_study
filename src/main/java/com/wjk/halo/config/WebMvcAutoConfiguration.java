package com.wjk.halo.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.core.PageJacksonSerializer;
import com.wjk.halo.factory.StringToEnumConverterFactory;
import com.wjk.halo.model.support.HaloConst;
import com.wjk.halo.security.resolver.AuthenticationArgumentResolver;
import freemarker.core.TemplateClassResolver;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.servlet.MultipartConfigElement;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.wjk.halo.model.support.HaloConst.FILE_SEPARATOR;
import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;
import static com.wjk.halo.utils.HaloUtils.ensureBoth;
import static com.wjk.halo.utils.HaloUtils.ensureSuffix;

/**
 * 如果一个配置类只配置@ConfigurationProperties注解，而没有使用@Component，
 * 那么在IOC容器中是获取不到properties 配置文件转化的bean
 * 因此@EnableConfigurationProperties 相当于把使用 @ConfigurationProperties 的类进行了一次注入。
 *
 */

@Slf4j
@Configuration
@EnableConfigurationProperties(MultipartProperties.class)
public class WebMvcAutoConfiguration extends WebMvcConfigurationSupport {

    private static final String FILE_PROTOCOL = "file:///";

    /**
     * 分页解析器，从请求参数中提取分页信息
     */
    private final PageableHandlerMethodArgumentResolver pageableResolver;

    /**
     * 排序解析器
     */
    private final SortHandlerMethodArgumentResolver sortResolver;

    private final HaloProperties haloProperties;

    public WebMvcAutoConfiguration(PageableHandlerMethodArgumentResolver pageableResolver,
                                   SortHandlerMethodArgumentResolver sortResolver,
                                   HaloProperties haloProperties) {
        this.pageableResolver = pageableResolver;
        this.sortResolver = sortResolver;
        this.haloProperties = haloProperties;
    }

    /**
     * 配置消息转换器
     * 添加自定义消息转换器不覆盖默认转换器
     * @param converters：已经配置好的消息转换器列表
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        /**
         * 遍历所有已经配置好的消息转换器列表
         * 过滤掉不属于MappingJackson2HttpMessageConverter的转换器
         * 找到第一个属于MappingJackson2HttpMessageConverter的转换器
         */
        converters.stream()
                .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                .findFirst()
                .ifPresent(converter -> {
                    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter) converter;
                    Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();   //获取一个Jackson2ObjectMapperBuilder实例
                    JsonComponentModule module = new JsonComponentModule();
                    module.addSerializer(PageImpl.class, new PageJacksonSerializer());
                    ObjectMapper objectMapper = builder.modules(module).build();
                    mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
                });
    }

    //增加参数解析器
    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        /**
         * 添加权限验证参数解析器
         */
        resolvers.add(new AuthenticationArgumentResolver());
        resolvers.add(pageableResolver);
        resolvers.add(sortResolver);
    }

    /**
     * Configuring static resource path
     *
     *  配置静态资源访问，避免静态资源被拦截
     *  addResourceHandler（） 添加的是访问路径
     *  addResourceLocations（）添加的是映射后的真实路径，映射的真实路径末尾必须加 /
     * @param registry registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // workDir :   file://用户根目录/.halo/
        String workDir = FILE_PROTOCOL + ensureSuffix(haloProperties.getWorkDir(), FILE_SEPARATOR);

        // register /** resource handler.
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/admin/")
                .addResourceLocations(workDir + "static/");

        /**
         * 使用 /themes/** 访问静态资源，真实的资源访问地址为 templates/themes/
         */
        // register /themes/** resource handler.
        registry.addResourceHandler("/themes/**")
                .addResourceLocations(workDir + "templates/themes/");

        // 返回： /upload/**
        String uploadUrlPattern = ensureBoth(haloProperties.getUploadUrlPrefix(), URL_SEPARATOR) + "**";
        // 返回： admin/**
        String adminPathPattern = ensureSuffix(haloProperties.getAdminPath(), URL_SEPARATOR) + "**";

        /**
         * 使用/upload/** 访问静态资源，真实的资源访问地址为 file://用户根目录/.halo/upload/
         */
        registry.addResourceHandler(uploadUrlPattern)
                .setCacheControl(CacheControl.maxAge(7L, TimeUnit.DAYS))    //设置返回头中的cache_control字段，缓存时间设为7天
                .addResourceLocations(workDir + "upload/");

        /**
         * 使用 admin/** 访问静态资源，真实的资源访问地址为 classpath:/admin/
         */
        registry.addResourceHandler(adminPathPattern)
                .addResourceLocations("classpath:/admin/");

        if (!haloProperties.isDocDisabled()) {
            // If doc is enable
            registry.addResourceHandler("swagger-ui.html")
                    .addResourceLocations("classpath:/META-INF/resources/");
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
    }


    @Override
    protected void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }


    /**
     * Configuring freemarker template file path.
     *
     * @return new FreeMarkerConfigurer
     */
    @Bean
    public FreeMarkerConfigurer freemarkerConfig(HaloProperties haloProperties) throws IOException, TemplateException {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPaths(FILE_PROTOCOL + haloProperties.getWorkDir() + "templates/", "classpath:/templates/");
        configurer.setDefaultEncoding("UTF-8");

        Properties properties = new Properties();
        properties.setProperty("auto_import", "/common/macro/common_macro.ftl as common,/common/macro/global_macro.ftl as global");

        configurer.setFreemarkerSettings(properties);

        // Predefine configuration
        freemarker.template.Configuration configuration = configurer.createConfiguration();

        configuration.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);

        if (haloProperties.isProductionEnv()) {
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }

        // Set predefined freemarker configuration
        configurer.setConfiguration(configuration);

        return configurer;
    }

    /**
     * Configuring multipartResolver for large file upload..
     *  处理文件上传
     * @return new multipartResolver
     */
    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver(MultipartProperties multipartProperties) {
        MultipartConfigElement multipartConfigElement = multipartProperties.createMultipartConfig();
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        resolver.setMaxUploadSize(multipartConfigElement.getMaxRequestSize());
        resolver.setMaxUploadSizePerFile(multipartConfigElement.getMaxFileSize());

        //lazy multipart parsing, throwing parse exceptions once the application attempts to obtain multipart files
        resolver.setResolveLazily(true);

        return resolver;
    }

    /**
     * Configuring view resolver
     *
     * 配置视图解析器
     * @param registry registry
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setAllowRequestOverride(false);
        resolver.setCache(false);
        resolver.setExposeRequestAttributes(false);
        resolver.setExposeSessionAttributes(false);
        resolver.setExposeSpringMacroHelpers(true);
        resolver.setSuffix(HaloConst.SUFFIX_FTL);
        resolver.setContentType("text/html; charset=UTF-8");
        registry.viewResolver(resolver);
    }


    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new HaloRequestMappingHandlerMapping(haloProperties);
    }
}
