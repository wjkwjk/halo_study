package com.wjk.halo.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.wjk.halo.model.dto.JournalDTO;
import com.wjk.halo.model.entity.Journal;
import com.wjk.halo.model.entity.JournalComment;
import com.wjk.halo.model.vo.JournalCommentWithJournalVO;
import com.wjk.halo.repository.JournalCommentRepository;
import com.wjk.halo.repository.JournalRepository;
import com.wjk.halo.repository.base.BaseCommentRepository;
import com.wjk.halo.service.JournalCommentService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.UserService;
import com.wjk.halo.utils.ServiceUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JournalCommentServiceImpl extends BaseCommentServiceImpl<JournalComment> implements JournalCommentService {

    private final JournalCommentRepository journalCommentRepository;

    private final JournalRepository journalRepository;

    public JournalCommentServiceImpl(JournalCommentRepository journalCommentRepository,
                                     OptionService optionService,
                                     UserService userService,
                                     ApplicationEventPublisher eventPublisher,
                                     JournalRepository journalRepository) {
        super(journalCommentRepository, optionService, userService, eventPublisher);
        this.journalCommentRepository = journalCommentRepository;
        this.journalRepository = journalRepository;
    }

    @Override
    public List<JournalCommentWithJournalVO> convertToWithJournalVo(List<JournalComment> journalComments) {
        if (CollectionUtil.isEmpty(journalComments)){
            return Collections.emptyList();
        }

        Set<Integer> journalIds = ServiceUtils.fetchProperty(journalComments, JournalComment::getPostId);

        List<Journal> journals = journalRepository.findAllById(journalIds);

        Map<Integer, Journal> journalMap = ServiceUtils.convertToMap(journals, Journal::getId);

        return journalComments.stream()
                .filter(journalComment -> journalMap.containsKey(journalComment.getPostId()))
                .map(journalComment -> {
                    JournalCommentWithJournalVO journalCommentWithJournalVO = new JournalCommentWithJournalVO().convertFrom(journalComment);
                    journalCommentWithJournalVO.setJournal(new JournalDTO().convertFrom(journalMap.get(journalComment.getPostId())));
                    return journalCommentWithJournalVO;
                }).collect(Collectors.toList());
    }

    @Override
    public Page<JournalCommentWithJournalVO> convertToWithJournalVo(Page<JournalComment> journalCommentPage) {
        List<JournalCommentWithJournalVO> journalCommentWithJournalVOS = convertToWithJournalVo(journalCommentPage.getContent());
        return new PageImpl<>(journalCommentWithJournalVOS, journalCommentPage.getPageable(), journalCommentPage.getTotalElements());
    }
}
