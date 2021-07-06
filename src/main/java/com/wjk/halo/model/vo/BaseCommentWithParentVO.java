package com.wjk.halo.model.vo;

import com.wjk.halo.model.dto.BaseCommentDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class BaseCommentWithParentVO extends BaseCommentDTO implements Cloneable {

    private BaseCommentWithParentVO parent;

    @Override
    public BaseCommentWithParentVO clone(){
        try {
            return (BaseCommentWithParentVO) super.clone();
        } catch (CloneNotSupportedException e){
            log.error("Clone not support exception", e);
            return null;
        }
    }
}
