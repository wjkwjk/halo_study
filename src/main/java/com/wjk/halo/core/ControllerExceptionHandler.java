package com.wjk.halo.core;

import com.wjk.halo.exception.AbstractHaloException;
import com.wjk.halo.model.support.BaseResponse;
import com.wjk.halo.utils.ExceptionUtils;
import com.wjk.halo.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

/**
 * 该类主要用于捕获异常，然后使得发生异常时返回的格式和正常返回格式相同
 */

/**
 * @RestControllerAdvice 注解，可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute，并应用到所有@RequestMapping中
 * 　主要配合@ExceptionHandler使用，统一处理异常情况
 *
 * 使用动机是：想要出现异常时的返回格式和正常返回时的格式相同，因此使用该接口来捕获指定的异常类型，进行格式转换或者其他操作
 *
 * RestControllerAdvice = ControllerAdvice + ResponseBody
 */

/**
 * @ExceptionHandler(Class)：用在方法上，使用该方法中处理括号中的异常类
 */

@RestControllerAdvice(value = {"com.wjk.halo.controller.admin.api", "com.wjk.halo.controller.content.api"})
@Slf4j
public class ControllerExceptionHandler {

    /**
     * DataIntegrityViolationException.class：在更新数据库时，出现了违反数据完整性的异常，例如，主键重复，列数据太长
     * @ResponseStatus(HttpStatus.BAD_REQUEST) ： 设置返回码
     * @return
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleDataIntegrityViolationException(DataIntegrityViolationException e){
        BaseResponse<?> baseResponse = handleBaseException(e);
        if (e.getCause() instanceof ConstraintViolationException){
            baseResponse = handleBaseException(e.getCause());
        }
        baseResponse.setMessage("字段验证错误，请完善后重试！");
        return baseResponse;
    }

    /**
     * 缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e){
        BaseResponse<?> baseResponse = handleBaseException(e);
        baseResponse.setMessage(String.format("请求字段缺失, 类型为 %s，名称为 %s", e.getParameterType(), e.getParameterName()));
        return baseResponse;
    }

    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleConstraintViolationException(javax.validation.ConstraintViolationException e){
        BaseResponse<Map<String, String>> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponse.setMessage("字段验证错误，请完善后重试！");
        baseResponse.setData(ValidationUtils.mapWithValidError(e.getConstraintViolations()));
        return baseResponse;
    }

    /**
     *方法参数异常，即请求中的参数不符合@Valid验证（model/params下的类，每个变量上注解，即是用于验证的）
     * 再返回信息中设置遗验证错误的字段以及对应的错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        BaseResponse<Map<String, String>> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponse.setMessage("字段验证错误，请完善后重试！");
        //将验证失败的字段以及对应的错误信息封装成map
        Map<String, String> errMap = ValidationUtils.mapWithFieldError(e.getBindingResult().getFieldErrors());
        baseResponse.setData(errMap);
        return baseResponse;
    }

    /**
     *请求方式不支持（例如POST,GET等）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e){
        BaseResponse<?> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return baseResponse;
    }

    /**
     * http请求报头[Request Headers]中的Accept字段，是否与服务器返回的响应报头[Response Headers]的Content-Type是否匹配，如不匹配，则会抛出该错误。
     * Accept表示浏览器期望请求得到资源类型
     * Content-Type表示服务器返回资源类型
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public BaseResponse<?> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e){
        BaseResponse<?> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        return baseResponse;
    }

    /**
     * 一般是有反序列化失败引起的，造成无法读取请求信息
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        baseResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        baseResponse.setMessage("缺失请求主体");
        return baseResponse;
    }

    /**
     * 请求找不到对应的处理方法，即出现404（相当于当前请求的api，没有在conroller中出现）
     * @param e
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public BaseResponse<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        baseResponse.setStatus(status.value());
        baseResponse.setMessage(status.getReasonPhrase());
        return baseResponse;
    }

    /**
     * 上传的文件大小限制
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleUploadSizeExceededException(MaxUploadSizeExceededException e) {
        BaseResponse<Object> response = handleBaseException(e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setMessage("当前请求超出最大限制：" + e.getMaxUploadSize() + " bytes");
        return response;
    }

    /**
     * 处理自定义的异常
     */
    @ExceptionHandler(AbstractHaloException.class)
    public ResponseEntity<BaseResponse<?>> handleHaloException(AbstractHaloException e) {
        BaseResponse<Object> baseResponse = handleBaseException(e);
        baseResponse.setStatus(e.getStatus().value());
        baseResponse.setData(e.getErrorData());
        return new ResponseEntity<>(baseResponse, e.getStatus());
    }

    /**
     * 处理其他的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<?> handleGlobalException(Exception e) {
        BaseResponse<?> baseResponse = handleBaseException(e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        baseResponse.setStatus(status.value());
        baseResponse.setMessage(status.getReasonPhrase());
        return baseResponse;
    }

    /**
     * 生成基本的返回体，因为目的是想要让发生异常的时的返回格式和普通的返回格式相同，因此首先生成的基本的返回格式，然后将异常数据放到里面
     * @param t
     * @param <T>
     * @return
     */
    private <T> BaseResponse<T> handleBaseException(Throwable t){
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.setMessage(t.getMessage());

        if (log.isDebugEnabled()){
            log.error("Captured an exception:", t);
            //将异常数据转换为String类型，然后放到返回返回体中
            baseResponse.setDevMessage(ExceptionUtils.getStackTrace(t));
        }else {
            log.error("Captured an exception: [{}]", t.getMessage());
        }
        return baseResponse;
    }


}
