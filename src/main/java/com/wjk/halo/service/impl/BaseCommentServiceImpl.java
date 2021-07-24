package com.wjk.halo.service.impl;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.wjk.halo.event.comment.CommentNewEvent;
import com.wjk.halo.event.comment.CommentReplyEvent;
import com.wjk.halo.model.dto.BaseCommentDTO;
import com.wjk.halo.model.entity.BaseComment;
import com.wjk.halo.model.enums.CommentStatus;
import com.wjk.halo.model.params.BaseCommentParam;
import com.wjk.halo.model.params.CommentQuery;
import com.wjk.halo.model.properties.CommentProperties;
import com.wjk.halo.model.vo.BaseCommentVO;
import com.wjk.halo.model.vo.BaseCommentWithParentVO;
import com.wjk.halo.model.vo.CommentWithHasChildrenVO;
import com.wjk.halo.repository.base.BaseCommentRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.security.authentication.Authentication;
import com.wjk.halo.security.context.SecurityContextHolder;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.UserService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.service.base.BaseCommentService;
import com.wjk.halo.utils.ServiceUtils;
import com.wjk.halo.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class BaseCommentServiceImpl<COMMENT extends BaseComment> extends AbstractCrudService<COMMENT, Long> implements BaseCommentService<COMMENT> {

    protected final OptionService optionService;
    protected final UserService userService;
    protected final ApplicationEventPublisher eventPublisher;
    private final BaseCommentRepository<COMMENT> baseCommentRepository;

    public BaseCommentServiceImpl(BaseCommentRepository<COMMENT> baseCommentRepository, OptionService optionService, UserService userService, ApplicationEventPublisher eventPublisher) {
        super(baseCommentRepository);
        this.optionService = optionService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.baseCommentRepository = baseCommentRepository;
    }

    @Override
    public long countByPostId(Integer postId) {
        return baseCommentRepository.countByPostId(postId);
    }

    @Override
    public List<COMMENT> listBy(Integer postId) {
        return baseCommentRepository.findAllByPostId(postId);
    }

    @Override
    public Page<COMMENT> pageLatest(int top) {
        return pageLatest(top, null);
    }

    @Override
    public Page<COMMENT> pageLatest(int top, CommentStatus status) {
        if (status == null){
            return listAll(ServiceUtils.buildLatestPageable(top));
        }
        return baseCommentRepository.findAllByStatus(status, ServiceUtils.buildLatestPageable(top));
    }

    @Override
    public Page<COMMENT> pageBy(CommentStatus status, Pageable pageable) {
        return null;
    }

    @Override
    public Page<COMMENT> pageBy(CommentQuery commentQuery, Pageable pageable) {
        return null;
    }

    @Override
    public Page<BaseCommentVO> pageVosAllBy(Integer postId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<BaseCommentVO> pageVosBy(Integer postId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<BaseCommentVO> pageVosBy(List<COMMENT> comments, Pageable pageable) {
        return null;
    }

    @Override
    public Page<BaseCommentWithParentVO> pageWithParentVoBy(Integer postId, Pageable pageable) {
        return null;
    }

    @Override
    public Map<Integer, Long> countByPostIds(Collection<Integer> postIds) {
        return null;
    }

    @Override
    public long countByStatus(CommentStatus status) {
        return 0;
    }

    @Override
    public COMMENT create(COMMENT comment) {
        if (!ServiceUtils.isEmptyId(comment.getPostId())){
            validateTarget(comment.getPostId());
        }

        if (!ServiceUtils.isEmptyId(comment.getParentId())){
            mustExistById(comment.getParentId());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (comment.getIpAddress() == null){
            comment.setIpAddress(ServletUtils.getRequestIp());
        }

        if (comment.getUserAgent() == null){
            comment.setUserAgent(ServletUtils.getHeaderIgnoreCase(HttpHeaders.USER_AGENT));
        }

        if (comment.getGravatarMd5() == null){
            comment.setGravatarMd5(DigestUtils.md5Hex(comment.getEmail()));
        }

        if (StringUtils.isNotEmpty(comment.getAuthorUrl())){
            comment.setAuthorUrl(URLUtil.normalize(comment.getAuthorUrl()));
        }

        if (authentication != null){
            comment.setIsAdmin(true);
            comment.setStatus(CommentStatus.PUBLISHED);
        }else {
            Boolean needAudit = optionService.getByPropertyOrDefault(CommentProperties.NEW_NEED_CHECK, Boolean.class, true);
            comment.setStatus(needAudit ? CommentStatus.AUDITING : CommentStatus.PUBLISHED);
        }

        COMMENT createdComment = super.create(comment);

        if (ServiceUtils.isEmptyId(createdComment.getParentId())){
            if (authentication == null){
                eventPublisher.publishEvent(new CommentNewEvent(this, createdComment.getId()));
            }
        }else {
            eventPublisher.publishEvent(new CommentReplyEvent(this, createdComment.getId()));
        }
        return createdComment;
    }

    @Override
    public COMMENT createBy(BaseCommentParam<COMMENT> commentParam) {
        return null;
    }

    @Override
    public COMMENT updateStatus(Long commentId, CommentStatus status) {
        return null;
    }

    @Override
    public List<COMMENT> updateStatusByIds(List<Long> ids, CommentStatus status) {
        return null;
    }

    @Override
    public List<COMMENT> removeByPostId(Integer postId) {
        return null;
    }



    @Override
    public List<COMMENT> removeByIds(Collection<Long> ids) {
        return null;
    }

    @Override
    public BaseCommentDTO convertTo(COMMENT comment) {
        return null;
    }

    @Override
    public List<BaseCommentDTO> convertTo(List<COMMENT> comments) {
        return null;
    }

    @Override
    public Page<BaseCommentDTO> convertTo(Page<COMMENT> comments) {
        return null;
    }

    @Override
    public List<BaseCommentVO> convertToVo(List<COMMENT> comments, Comparator<BaseCommentVO> comparator) {
        return null;
    }

    @Override
    public Page<CommentWithHasChildrenVO> pageTopCommentsBy(Integer targetId, CommentStatus status, Pageable pageable) {
        return null;
    }

    @Override
    public void validateTarget(Integer targetId) {

    }

    @Override
    public List<COMMENT> listChildrenBy(Integer targetId, Long commentParentId, CommentStatus status, Sort sort) {
        return null;
    }

    @Override
    public List<COMMENT> listChildrenBy(Integer targetId, Long commentParentId, Sort sort) {
        return null;
    }

    @Override
    public <T extends BaseCommentDTO> T filterIpAddress(T comment) {
        return null;
    }

    @Override
    public <T extends BaseCommentDTO> List<T> filterIpAddress(List<T> comments) {
        return null;
    }

    @Override
    public <T extends BaseCommentDTO> Page<T> filterIpAddress(Page<T> commentPage) {
        return null;
    }

    @Override
    public List<BaseCommentDTO> replaceUrl(String oldUrl, String newUrl) {
        return null;
    }
}
