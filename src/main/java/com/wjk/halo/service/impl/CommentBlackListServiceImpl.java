package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.CommonBlackList;
import com.wjk.halo.model.enums.CommentViolationTypeEnum;
import com.wjk.halo.model.properties.CommentProperties;
import com.wjk.halo.repository.CommentBlackListRepository;
import com.wjk.halo.repository.PostCommentRepository;
import com.wjk.halo.service.CommentBlackListService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class CommentBlackListServiceImpl extends AbstractCrudService<CommonBlackList, Long> implements CommentBlackListService {

    private final CommentBlackListRepository commentBlackListRepository;
    private final PostCommentRepository postCommentRepository;
    private final OptionService optionService;

    public CommentBlackListServiceImpl(CommentBlackListRepository commentBlackListRepository, PostCommentRepository postCommentRepository, OptionService optionService) {
        super(commentBlackListRepository);
        this.commentBlackListRepository = commentBlackListRepository;
        this.postCommentRepository = postCommentRepository;
        this.optionService = optionService;
    }

    @Override
    public CommentViolationTypeEnum commentsBanStatus(String ipAddress) {
        Optional<CommonBlackList> blackList = commentBlackListRepository.findByIpAddress(ipAddress);
        LocalDateTime now = LocalDateTime.now();
        Date endTime = new Date(DateTimeUtils.toEpochMilli(now));
        Integer banTime = optionService.getByPropertyOrDefault(CommentProperties.COMMENT_BAN_TIME, Integer.class, 10);
        Date startTime  = new Date(DateTimeUtils.toEpochMilli(now.minusMinutes(banTime)));
        Integer range = optionService.getByPropertyOrDefault(CommentProperties.COMMENT_RANGE, Integer.class, 30);
        boolean isPresent = postCommentRepository.countByIpAndTime(ipAddress, startTime, endTime) >= range;
        if (isPresent && blackList.isPresent()){
            update(now, blackList.get(), banTime);
            return CommentViolationTypeEnum.FREQUENTLY;
        }else if (isPresent){
            CommonBlackList commentBlackList = CommonBlackList
                    .builder()
                    .banTime(getBanTime(now, banTime))
                    .ipAddress(ipAddress)
                    .build();
            super.create(commentBlackList);
            return CommentViolationTypeEnum.FREQUENTLY;
        }
        return CommentViolationTypeEnum.NORMAL;
    }

    private void update(LocalDateTime localDateTime, CommonBlackList blackList, Integer banTime){
        blackList.setBanTime(getBanTime(localDateTime, banTime));
        int updateResult = commentBlackListRepository.updateByIpAddress(blackList);
        Optional.of(updateResult)
                .filter(result -> result<=0).ifPresent(result -> log.error("更新评论封禁时间失败"));
    }

    private Date getBanTime(LocalDateTime localDateTime, Integer banTime){
        return new Date(DateTimeUtils.toEpochMilli(localDateTime.plusMinutes(banTime)));
    }

}
