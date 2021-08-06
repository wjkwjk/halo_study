package com.wjk.halo.service.impl;

import com.wjk.halo.exception.AlreadyExistsException;
import com.wjk.halo.handler.file.FileHandlers;
import com.wjk.halo.model.dto.AttachmentDTO;
import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.model.enums.AttachmentType;
import com.wjk.halo.model.params.AttachmentQuery;
import com.wjk.halo.model.properties.AttachmentProperties;
import com.wjk.halo.model.support.UploadResult;
import com.wjk.halo.repository.AttachmentRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.AttachmentService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.HaloUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.stream.Collectors;

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

        return attachmentPage.map(this::convertToDto);
    }

    @Override
    public AttachmentDTO convertToDto(Attachment attachment) {
        String blogBaseUrl = optionService.getBlogBaseUrl();

        Boolean enabledAbsolutePath = optionService.isEnabledAbsolutePath();

        AttachmentDTO attachmentDTO = new AttachmentDTO().convertFrom(attachment);

        if (Objects.equals(attachmentDTO.getType(), AttachmentType.LOCAL)){
            String fullPath = StringUtils.join(enabledAbsolutePath ? blogBaseUrl : "", "/", attachmentDTO.getPath());
            String fullThumbPath = StringUtils.join(enabledAbsolutePath ? blogBaseUrl : "", "/", attachmentDTO.getThumbPath());

            attachmentDTO.setPath(fullPath);
            attachmentDTO.setThumbPath(fullThumbPath);

        }
        return attachmentDTO;
    }

    @Override
    public Attachment removePermanently(Integer id) {
        Attachment deletedAttachment = removeById(id);

        fileHandlers.delete(deletedAttachment);

        log.debug("Deleted attachment: [{}]", deletedAttachment);

        return deletedAttachment;
    }

    @Override
    public List<Attachment> removePermanently(Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return ids.stream().map(this::removePermanently).collect(Collectors.toList());
    }

    @Override
    public Attachment upload(MultipartFile file) {
        AttachmentType attachmentType = getAttachmentType();
        //先获取附件的上传地址类型（本地或者是其他云服务器）
        log.debug("Starting uploading... type: [{}], file: [{}]", attachmentType, file.getOriginalFilename());

        // 上传文件
        UploadResult uploadResult = fileHandlers.upload(file, attachmentType);

        log.debug("Attachment type: [{}]", attachmentType);
        log.debug("Upload result: [{}]", uploadResult);

        Attachment attachment = new Attachment();
        attachment.setName(uploadResult.getFilename());

        attachment.setPath(HaloUtils.changeFileSeparatorToUrlSeparator(uploadResult.getFilePath()));
        attachment.setFileKey(uploadResult.getKey());
        attachment.setThumbPath(uploadResult.getThumbPath());
        attachment.setMediaType(uploadResult.getMediaType().toString());
        attachment.setSuffix(uploadResult.getSuffix());
        attachment.setWidth(uploadResult.getWidth());
        attachment.setHeight(uploadResult.getHeight());
        attachment.setSize(uploadResult.getSize());
        attachment.setType(attachmentType);

        log.debug("Creating attachment: [{}]", attachment);

        return create(attachment);
    }

    @Override
    public List<String> listAllMediaType() {
        return attachmentRepository.findAllMediaType();
    }

    @Override
    public List<AttachmentType> listAllType() {
        return attachmentRepository.findAllType();
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
                //建立模糊查询字符串
                String likeCondition = String.format("%%%s%%", StringUtils.strip(attachmentQuery.getKeyword()));

                Predicate nameLike = criteriaBuilder.like(root.get("name"), likeCondition);

                predicates.add(criteriaBuilder.or(nameLike));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

    @NonNull
    private AttachmentType getAttachmentType(){
        return Objects.requireNonNull(optionService.getEnumByPropertyOrDefault(AttachmentProperties.ATTACHMENT_TYPE, AttachmentType.class, AttachmentType.LOCAL));
    }

}
