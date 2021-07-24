package com.wjk.halo.service;

import com.wjk.halo.model.entity.CommentBlackList;
import com.wjk.halo.model.enums.CommentViolationTypeEnum;
import com.wjk.halo.service.base.CrudService;

public interface CommentBlackListService extends CrudService<CommentBlackList, Long> {
    CommentViolationTypeEnum commentsBanStatus(String ipAddress);
}
