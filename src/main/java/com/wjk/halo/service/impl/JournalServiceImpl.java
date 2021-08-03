package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.JournalDTO;
import com.wjk.halo.model.dto.JournalWithCmtCountDTO;
import com.wjk.halo.model.entity.Journal;
import com.wjk.halo.model.entity.JournalComment;
import com.wjk.halo.model.params.JournalParam;
import com.wjk.halo.model.params.JournalQuery;
import com.wjk.halo.repository.JournalRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.JournalCommentService;
import com.wjk.halo.service.JournalService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.MarkdownUtils;
import com.wjk.halo.utils.ServiceUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JournalServiceImpl extends AbstractCrudService<Journal, Integer> implements JournalService {

    private final JournalRepository journalRepository;

    private final JournalCommentService journalCommentService;

    public JournalServiceImpl(JournalRepository journalRepository, JournalCommentService journalCommentService) {
        super(journalRepository);
        this.journalRepository = journalRepository;
        this.journalCommentService = journalCommentService;
    }

    @Override
    public Page<Journal> pageBy(JournalQuery journalQuery, Pageable pageable) {
        return journalRepository.findAll(buildSpecByQuery(journalQuery), pageable);
    }

    @Override
    public Page<JournalWithCmtCountDTO> convertToCmtCountDto(Page<Journal> journalPage) {
        List<JournalWithCmtCountDTO> journalWithCmtCountDTOS = convertToCmtCountDto(journalPage.getContent());
        return new PageImpl<>(journalWithCmtCountDTOS, journalPage.getPageable(), journalPage.getTotalElements());
    }

    @Override
    public List<JournalWithCmtCountDTO> convertToCmtCountDto(List<Journal> journals) {
        if (CollectionUtils.isEmpty(journals)){
            return Collections.emptyList();
        }

        Set<Integer> journalIds = ServiceUtils.fetchProperty(journals, Journal::getId);

        Map<Integer, Long> journalCommentCountMap = journalCommentService.countByPostIds(journalIds);

        return journals.stream()
                .map(journal -> {
                    JournalWithCmtCountDTO journalWithCmtCountDTO = new JournalWithCmtCountDTO().convertFrom(journal);

                    journalWithCmtCountDTO.setCommentCount(journalCommentCountMap.getOrDefault(journal.getId(), 0L));
                    return journalWithCmtCountDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<Journal> pageLatest(int top) {
        return listAll(ServiceUtils.buildLatestPageable(top));
    }

    @Override
    public Journal createBy(JournalParam journalParam) {
        Journal journal = journalParam.convertTo();
        journal.setContent(MarkdownUtils.renderHtml(journal.getSourceContent()));

        return create(journal);
    }

    @Override
    public JournalDTO convertTo(Journal journal) {
        return new JournalDTO().convertFrom(journal);
    }

    @Override
    public Journal updateBy(Journal journal) {
        journal.setContent(MarkdownUtils.renderHtml(journal.getSourceContent()));
        return update(journal);
    }


    @NonNull
    private Specification<Journal> buildSpecByQuery(@NonNull JournalQuery journalQuery){
        return (Specification<Journal>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (journalQuery.getType() != null){
                predicates.add(criteriaBuilder.equal(root.get("type"), journalQuery.getType()));
            }

            if (journalQuery.getKeyword() != null){
                String likeCondition = String.format("%%%s%%", StringUtils.strip(journalQuery.getKeyword()));

                Predicate contentLike = criteriaBuilder.like(root.get("content"), likeCondition);

                predicates.add(criteriaBuilder.or(contentLike));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

    @Override
    public Journal removeById(Integer id) {

        List<JournalComment> journalComments = journalCommentService.removeByPostId(id);
        log.debug("Removed journal comments: [{}]", journalComments);

        return super.removeById(id);
    }
}
