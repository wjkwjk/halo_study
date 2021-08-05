package com.wjk.halo.service.impl;

import com.wjk.halo.exception.AlreadyExistsException;
import com.wjk.halo.handler.file.FileHandlers;
import com.wjk.halo.model.dto.AttachmentDTO;
import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.model.params.AttachmentQuery;
import com.wjk.halo.repository.AttachmentRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.AttachmentService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.base.AbstractCrudService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;

@Slf4j
@Service
public class AttachmentServiceImpl extends AbstractCrudService<Attachment, Integer> implements AttachmentService {

    private final AttachmentRepository attachmentRepository;

    private OptionService optionService;

    private FileHandlers fileHandlers;


    public AttachmentServiceImpl(AttachmentRepository attachmentRepository, OptionService optionService, FileHandlers fileHandlers) {
        super(attachmentRepository);
        this.attachmentRepository = attachmentRepository;
        this.optionService = optionService;
        this.fileHandlers = fileHandlers;
    }

    @Override
    public Attachment create(Attachment attachment) {
        pathMustNotExist(attachment);
        return super.create(attachment);
    }

    private void pathMustNotExist(@NonNull Attachment attachment){
        long pathCount = attachmentRepository.countByPath(attachment.getPath());

        if (pathCount > 0){
            throw new AlreadyExistsException("附件路径为 " + attachment.getPath() + " 已经存在");
        }
    }

    @Override
    public Page<AttachmentDTO> pageDtosBy(@NonNull Pageable pageable, AttachmentQuery attachmentQuery) {
        Page<Attachment> attachmentPage = attachmentRepository.findAll(buildSpecByQuery(attachmentQuery), pageable);

        return attachmentPage.map(this::con)
    }

    @NonNull
    private Specification<Attachment> buildSpecByQuery(@NonNull AttachmentQuery attachmentQuery){
        return (Specification<Attachment>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (attachmentQuery.getMediaType() != null){
                predicates.add(criteriaBuilder.equal(root.get("mediaType"), attachmentQuery.getMediaType()));
            }

            if (attachmentQuery.getAttachmentType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), attachmentQuery.getAttachmentType()));
            }

            if (attachmentQuery.getKeyword() != null) {

                String likeCondition = String.format("%%%s%%", StringUtils.strip(attachmentQuery.getKeyword()));

                Predicate nameLike = criteriaBuilder.like(root.get("name"), likeCondition);

                predicates.add(criteriaBuilder.or(nameLike));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

}
