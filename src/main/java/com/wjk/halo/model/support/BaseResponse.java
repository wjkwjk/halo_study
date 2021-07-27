package com.wjk.halo.model.support;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Null;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

    private Integer status;

    private String message;

    private String devMessage;

    private T data;

    public BaseResponse(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    @NonNull
    public static <T> BaseResponse<T> ok(@NonNull T data){
        return new BaseResponse<T>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    @NonNull
    public static <T> BaseResponse<T> ok(@Nullable String message){
        return ok(message, null);
    }

    @NonNull
    public static <T> BaseResponse<T> ok(@Nullable String message, @Nullable T data){
        return new BaseResponse<>(HttpStatus.OK.value(), message, data);
    }

}
