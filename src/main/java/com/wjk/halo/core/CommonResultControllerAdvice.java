package com.wjk.halo.core;

import com.wjk.halo.model.support.BaseResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 向前段返回结果时，进行的切面的方法
 * ResponseBodyAdvice
 *      实现这个接口的类，可以修改返回值直接作为 ResponseBody类型接口的返回值。
 *      有两种类型的处理器会将返回值作为ResponseBody：
 *          返回值为HpptEntity
 *          加了@ResponseBody注解
 * 但是仅仅实现该接口并不会生效，想要使这些实现ResponseBodyAdvice接口的类生效，有两种方式：
 *      在类上直接加上@ControllerAdvice注解
 *      注册到RequestMappingHandlerAdapter中
 */

@ControllerAdvice("com.wjk.halo.controller")
public class CommonResultControllerAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 只有当supports返回true，才会进入到beforeBodyWrite方法内
     * 否则，不进行处理，直接返回给前端
     * springmvc中总共定义了消息转换器，用于将返回值转换到指定类型，在返回给前端，例如json,xml,text
     *  大致流程为：
     *      1、根据返回值获取其类型，其中MethodParameter封装了方法对象，可获去方法返回值类型。
     *      2、根据request的Accept和HandellerMapping的produces属性经过比对、排序从而得到最应该转换的消息格式（MediaType）
     *        request的Accept表示前端希望获取到的返回值的格式
     *      3、遍历所有已配置的消息转换器，调用其canWrite方法，根据返回值类型（valueType）和消息格式（MediaType）来检测是否可以转换。
     *      4、若有对应的消息转换器，则进行转换，否则抛出异常
     *
     * 该方法判断返回的格式是否可以转换为json，只有能够转换为json的，才会调用beforeBodyWrite处理
     */
    @Override
    public boolean supports(MethodParameter returnType, @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        //判断返回数据是否可以背转换为json
        return AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    /**
     * 当客户端向服务器端请求数据，服务器端需要决定以何种形式将数据返回给客户端，这个决定的过程就叫做内容协商(Content Negotiation)。
     *
     * 服务器端决定以何种形式来返回数据依赖内容协商策略(ContentNegotiationStrategy)，Spring有默认的策略，也可以通过配置自定义。
     *
     * 正常HTTP协议的工作方式是通过Accept header来决定返回数据的类型，但是这种方式太过于依赖客户端的请求参数(Accept header)，有些时候用起来不太方便，
     * 所以Spring默认定义了一些列的内容协商策略(包括原生HTTP Accept header的方式)。
     * Spring定义了默认的内容协商策略：
     *  第一是URL中路径的后缀(Path Extension Strategy)。如果后缀是.html，则返回HTML格式数据；如果后缀是.xls，则返回Excel格式数据。默认这种方式是开启的。
     *  第二是URL参数format(可以自定义)(Parameter Strategy)。例如：http://myserver/myapp/accounts/list?format=xls，Spring会根据format的定义来决定返回数据的格式。默认这种方式关闭的。
     *  最后一个是Accept header(Header Strategy)。这个是真正HTTP工作的方式。默认这种方式是开启的。
     *
     *body参数：即返回体
     */

    @Override
    @NonNull
    public Object beforeBodyWrite(@Nullable Object body,
                                  @NotNull MethodParameter returnType,
                                  @NotNull MediaType contentType,
                                  @NotNull Class<? extends HttpMessageConverter<?>> converterType,
                                  @NotNull ServerHttpRequest request,
                                  @NotNull ServerHttpResponse response) {
        MappingJacksonValue container = getOrCreateContainer(body);

        beforeBodyWriteInternal(container, contentType, returnType, request, response);
        return container;
    }

    /**
     * MappingJacksonValue:用于包装待序列化对象
     * new MappingJacksonValue(body):body为待序列化对象
     * @param body
     * @return
     */
    private MappingJacksonValue getOrCreateContainer(Object body){
        return body instanceof MappingJacksonValue ? (MappingJacksonValue) body : new MappingJacksonValue(body);
    }

    private void beforeBodyWriteInternal(MappingJacksonValue bodyContainer,
                                         MediaType contentType,
                                         MethodParameter returnType,
                                         ServerHttpRequest request,
                                         ServerHttpResponse response){
        //获取待序列化对象
        Object returnBody = bodyContainer.getValue();
        if (returnBody instanceof BaseResponse){
            BaseResponse<?> baseResponse = (BaseResponse<?>) returnBody;
            response.setStatusCode(HttpStatus.resolve(baseResponse.getStatus()));
            return;
        }

        BaseResponse<?> baseResponse = BaseResponse.ok(returnBody);
        bodyContainer.setValue(baseResponse);
        response.setStatusCode(HttpStatus.valueOf(baseResponse.getStatus()));

    }

}
