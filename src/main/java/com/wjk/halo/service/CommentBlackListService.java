package com.wjk.halo.service;

import com.wjk.halo.model.entity.CommonBlackList;
import com.wjk.halo.model.enums.CommentViolationTypeEnum;
import com.wjk.halo.service.base.CrudService;

public interface CommentBlackListService extends CrudService<CommonBlackList, Long> {
    CommentViolationTypeEnum commentsBanStatus(String ipAddress);
}
