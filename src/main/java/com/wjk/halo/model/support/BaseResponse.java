package com.wjk.halo.model.support;

import lombok.*;
import org.springframework.http.HttpStatus;

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

    public static <T> BaseResponse<T> ok(@NonNull T data){
        return new BaseResponse<T>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

}
