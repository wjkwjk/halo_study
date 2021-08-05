package com.wjk.halo.service.impl;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.wjk.halo.event.comment.CommentNewEvent;
import com.wjk.halo.event.comment.CommentReplyEvent;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.dto.BaseCommentDTO;
import com.wjk.halo.model.entity.BaseComment;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.CommentStatus;
import com.wjk.halo.model.params.BaseCommentParam;
import com.wjk.halo.model.params.CommentQuery;
import com.wjk.halo.model.projection.CommentChildrenCountProjection;
import com.wjk.halo.model.projection.CommentCountProjection;
import com.wjk.halo.model.properties.BlogProperties;
import com.wjk.halo.model.properties.CommentProperties;
import com.wjk.halo.model.support.CommentPage;
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
import com.wjk.halo.utils.ValidationUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

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
        return baseCommentRepository.findAllByStatus(status, pageable);
    }

    @Override
    public Page<COMMENT> pageBy(CommentQuery commentQuery, Pageable pageable) {
        return baseCommentRepository.findAll(buildSpecByQuery(commentQuery), pageable);
    }

    @Override
    public Page<BaseCommentVO> pageVosAllBy(Integer postId, Pageable pageable) {
        log.debug("Getting comment tree view of post: [{}], page info: [{}]", postId, pageable);
        List<COMMENT> comments = baseCommentRepository.findAllByPostId(postId);
        return pageVosBy(comments, pageable);
    }

    @Override
    public Page<BaseCommentVO> pageVosBy(Integer postId, Pageable pageable) {
        log.debug("Getting comment tree view of post: [{}], page info: [{}]", postId, pageable);
        List<COMMENT> comments = baseCommentRepository.findAllByPostId(postId);
        return pageVosBy(comments, pageable);
    }

    @Override
    public Page<BaseCommentVO> pageVosBy(List<COMMENT> comments, Pageable pageable) {
        //如何对评论进行排序的比较器
        Comparator<BaseCommentVO> commentComparator = buildCommentComparator(pageable.getSortOr(Sort.by(Sort.Direction.DESC, "createTime")));
        //建立评论树，返回根评论
        List<BaseCommentVO> topComments = convertToVo(comments, commentComparator);

        List<BaseCommentVO> pageContent;

        int startIndex = pageable.getPageNumber() * pageable.getPageSize();
        if (startIndex >= topComments.size() || startIndex < 0){
            pageContent = Collections.emptyList();
        }else {
            int endIndex = startIndex + pageable.getPageSize();
            if (endIndex > topComments.size()){
                endIndex = topComments.size();
            }


            log.debug("Top comments size: [{}]", topComments.size());
            log.debug("Start index: [{}]", startIndex);
            log.debug("End index: [{}]", endIndex);

            pageContent = topComments.subList(startIndex, endIndex);

        }

        return new CommentPage<>(pageContent, pageable, topComments.size(), comments.size());

    }

    protected Comparator<BaseCommentVO> buildCommentComparator(Sort sort){
        return (currentComment, toCompareComment) -> {
            Sort.Order order = sort.filter(anOrder -> "id".equals(anOrder.getProperty()))
                    .get()
                    .findFirst()
                    .orElseGet(() -> Sort.Order.desc("id"));

            int sign = order.getDirection().isAscending() ? 1 : -1;

            return sign * currentComment.getId().compareTo(toCompareComment.getId());
        };
    }

    @Override
    public Page<BaseCommentWithParentVO> pageWithParentVoBy(Integer postId, Pageable pageable) {
        log.debug("Getting comment list view of post: [{}], page info: [{}]", postId, pageable);

        Page<COMMENT> commentPage = baseCommentRepository.findAllByPostIdAndStatus(postId, CommentStatus.PUBLISHED, pageable);

        List<COMMENT> comments = commentPage.getContent();

        //获得全部评论的全部的父评论集合
        Set<Long> parentIds = ServiceUtils.fetchProperty(comments, COMMENT::getParentId);

        List<COMMENT> parentComments = baseCommentRepository.findAllByIdIn(parentIds, pageable.getSort());

        Map<Long, COMMENT> parentCommentMap = ServiceUtils.convertToMap(parentComments, COMMENT::getId);

        Map<Long, BaseCommentWithParentVO> parentCommentVoMap = new HashMap<>(parentCommentMap.size());

        return commentPage.map(comment -> {
            BaseCommentWithParentVO commentWithParentVO = new BaseCommentWithParentVO().convertFrom(comment);

            BaseCommentWithParentVO parentCommentVo = parentCommentVoMap.get(comment.getParentId());

            if (parentCommentVo == null){
                COMMENT parentComment = parentCommentMap.get(comment.getParentId());

                if (parentComment != null){
                    parentCommentVo = new BaseCommentWithParentVO().convertFrom(parentComment);
                    parentCommentVoMap.put(parentComment.getId(), parentCommentVo);
                }
            }
            commentWithParentVO.setParent(parentCommentVo == null ? null : parentCommentVo.clone());

            return commentWithParentVO;
        });
    }

    @Override
    public Map<Integer, Long> countByPostIds(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)){
            return Collections.emptyMap();
        }
        List<CommentCountProjection> commentCountProjections = baseCommentRepository.countByPostIds(postIds);

        return ServiceUtils.convertToMap(commentCountProjections, CommentCountProjection::getPostId, CommentCountProjection::getCount);
    }

    @Override
    public long countByStatus(CommentStatus status) {
        return baseCommentRepository.countByStatus(status);
    }

    @Override
    public COMMENT create(COMMENT comment) {
        //判断post是否存在或者post是否允许评论
        if (!ServiceUtils.isEmptyId(comment.getPostId())){
            validateTarget(comment.getPostId());
        }

        //必须存在父评论
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null){
            User user = authentication.getDetail().getUser();
            commentParam.setAuthor(StringUtils.isBlank(user.getNickname()) ? user.getUsername() : user.getNickname());
            commentParam.setEmail(user.getEmail());
            commentParam.setAuthorUrl(optionService.getByPropertyOrDefault(BlogProperties.BLOG_URL, String.class, null));
        }

        ValidationUtils.validate(commentParam);

        if (authentication == null){
            if (userService.getByEmail(commentParam.getEmail()).isPresent()){
                throw new BadRequestException("不能使用博主的邮箱，如果您是博主，请登录管理端进行回复。");
            }
        }

        return create(commentParam.convertTo());

    }

    @Override
    public COMMENT updateStatus(Long commentId, CommentStatus status) {
        COMMENT comment = getById(commentId);

        comment.setStatus(status);

        return update(comment);
    }

    @Override
    public List<COMMENT> updateStatusByIds(List<Long> ids, CommentStatus status) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return ids.stream().map(id -> {
            return updateStatus(id, status);
        }).collect(Collectors.toList());
    }

    @Override
    public List<COMMENT> removeByPostId(Integer postId) {
        return baseCommentRepository.deleteByPostId(postId);
    }

    @Override
    public COMMENT removeById(Long id) {
        COMMENT comment = baseCommentRepository.findById(id).orElseThrow(() -> new NotFoundException("查询不到该评论的信息").setErrorData(id));

        //找到指定post以及指定父评论节点的全部子评论
        List<COMMENT> children = listChildrenBy(comment.getPostId(), id, Sort.by(Sort.Direction.DESC, "createTime"));

        //删除子评论
        if (children.size() >0){
            children.forEach(child -> {
                super.removeById(child.getId());
            });
        }
        //删除当前评论
        return super.removeById(id);

    }

    @Override
    public List<COMMENT> removeByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return ids.stream().map(this::removeById).collect(Collectors.toList());
    }

    @Override
    public BaseCommentDTO convertTo(COMMENT comment) {
        return new BaseCommentDTO().convertFrom(comment);
    }

    @Override
    public List<BaseCommentDTO> convertTo(List<COMMENT> comments) {
        if (CollectionUtils.isEmpty(comments)){
            return Collections.emptyList();
        }
        return comments.stream()
                .map(this::convertTo)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BaseCommentDTO> convertTo(Page<COMMENT> commentPage) {
        return commentPage.map(this::convertTo);
    }

    @NonNull
    @Override
    public List<BaseCommentVO> convertToVo(@Nullable List<COMMENT> comments, @Nullable Comparator<BaseCommentVO> comparator) {
        if (CollectionUtils.isEmpty(comments)){
            return Collections.emptyList();
        }
        //建立根节点，即虚拟一个根评论
        BaseCommentVO topVirtualComment = new BaseCommentVO();
        topVirtualComment.setId(0L);
        topVirtualComment.setChildren(new LinkedList<>());
        //对所有节点建立树关系
        concreteTree(topVirtualComment, new LinkedList<>(comments), comparator);

        return topVirtualComment.getChildren();

    }

    protected void concreteTree(@NonNull BaseCommentVO parentComment,
                                @Nullable Collection<COMMENT> comments,
                                @Nullable Comparator<BaseCommentVO> commentComparator){
        if (CollectionUtils.isEmpty(comments)){
            return;
        }
        //找出当前评论的子评论
        List<COMMENT> children = comments.stream()
                .filter(comment -> Objects.equals(parentComment.getId(), comment.getParentId()))
                .collect(Collectors.toList());
        //将子评论放到当前评论的子评论链表中
        children.forEach(comment -> {
            BaseCommentVO commentVO = new BaseCommentVO().convertFrom(comment);
            if (parentComment.getChildren() == null){
                parentComment.setChildren(new LinkedList<>());
            }
            parentComment.getChildren().add(commentVO);
        });
        //删除已经入树的评论
        comments.removeAll(children);
        //递归，给每一个评论建立子树
        if (!CollectionUtils.isEmpty(parentComment.getChildren())){
            parentComment.getChildren().forEach(childComment -> concreteTree(childComment, comments, commentComparator));
            //将每个节点的子评论排序
            if (commentComparator != null){
                parentComment.getChildren().sort(commentComparator);
            }
        }

    }

    @Override
    public Page<CommentWithHasChildrenVO> pageTopCommentsBy(Integer targetId, CommentStatus status, Pageable pageable) {
        Page<COMMENT> topCommentPage = baseCommentRepository.findAllByPostIdAndStatusAndParentId(targetId, status, 0L, pageable);
        if (topCommentPage.isEmpty()){
            return ServiceUtils.buildEmptyPageImpl(topCommentPage);
        }

        Set<Long> topCommentIds = ServiceUtils.fetchProperty(topCommentPage.getContent(), BaseComment::getId);

        List<CommentChildrenCountProjection> directChildrenCount = baseCommentRepository.findDirectChildrenCount(topCommentIds);

        Map<Long, Long> commentChildrenCountMap = ServiceUtils.convertToMap(directChildrenCount, CommentChildrenCountProjection::getCommentId, CommentChildrenCountProjection::getDirectChildrenCount);

        return topCommentPage.map(topComment -> {
            CommentWithHasChildrenVO comment = new CommentWithHasChildrenVO().convertFrom(topComment);
            comment.setHasChildren(commentChildrenCountMap.getOrDefault(topComment.getId(), 0L) > 0);
            return comment;
        });
    }


    @Override
    public List<COMMENT> listChildrenBy(Integer targetId, Long commentParentId, CommentStatus status, Sort sort) {

        List<COMMENT> directChildren = baseCommentRepository.findAllByPostIdAndStatusAndParentId(targetId, status, commentParentId);

        Set<COMMENT> children = new HashSet<>();

        getChildrenRecursively(directChildren, status, children);

        List<COMMENT> childrenList = new ArrayList<>(children);
        childrenList.sort(Comparator.comparing(BaseComment::getId));

        return childrenList;

    }

    @Override
    public List<COMMENT> listChildrenBy(Integer targetId, Long commentParentId, Sort sort) {
        //找到指定post以及指定父评论的所有直接子评论
        List<COMMENT> directChildren = baseCommentRepository.findAllByPostIdAndParentId(targetId, commentParentId);

        Set<COMMENT> children = new HashSet<>();
        //递归找到所有的子评论
        getChildrenRecursively(directChildren, children);

        List<COMMENT> childrenList = new ArrayList<>(children);
        childrenList.sort(Comparator.comparing(BaseComment::getId));

        return childrenList;

    }

    private void getChildrenRecursively(@Nullable List<COMMENT> topComments, @org.springframework.lang.NonNull CommentStatus status, @org.springframework.lang.NonNull Set<COMMENT> children){
        if (CollectionUtils.isEmpty(topComments)){
            return;
        }

        Set<Long> commentIds = ServiceUtils.fetchProperty(topComments, COMMENT::getId);

        List<COMMENT> directChildren = baseCommentRepository.findAllByStatusAndParentIdIn(status, commentIds);

        getChildrenRecursively(directChildren, status, children);

        children.addAll(topComments);
    }

    private void getChildrenRecursively(@Nullable List<COMMENT> topComments, @NonNull Set<COMMENT> children){
        if (CollectionUtils.isEmpty(topComments)){
            return;
        }

        Set<Long> commentIds = ServiceUtils.fetchProperty(topComments, COMMENT::getId);

        List<COMMENT> directChildren = baseCommentRepository.findAllByParentIdIn(commentIds);

        getChildrenRecursively(directChildren, children);

        children.addAll(topComments);
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

    //建立复杂查询，包含了多条件查询和模糊查询
    @NonNull
    protected Specification<COMMENT> buildSpecByQuery(@NonNull CommentQuery commentQuery){
        //return的部分实现了Specification中的toPredicate接口
        //root：从实体类中获取相应的字段
        //query：该接口定义了顶级查询功能，它包含着查询到各个部分，如：select、from、where、group by、order by 等
        //CriteriaBuilder：它是 CriteriaQuery 的工厂类，用于构建 JPA 安全查询。
        //在下面，首先使用CriteriaBuilder创建where条件，然后调用query.where生成where语句
        return (Specification<COMMENT>) (root, query, criteriaBuilder) -> {
            //Predicate: 过滤条件,相当于构造 where 中的条件
            List<Predicate> predicates = new LinkedList<>();
            //匹配status
            if (commentQuery.getStatus() != null){
                predicates.add(criteriaBuilder.equal(root.get("status"), commentQuery.getStatus()));
            }
            //匹配关键字
            if (commentQuery.getKeyword() != null){
                String likeCondition = String.format("%%%s%%", StringUtils.strip(commentQuery.getKeyword()));

                Predicate authorLike = criteriaBuilder.like(root.get("author"), likeCondition);
                Predicate contentLike = criteriaBuilder.like(root.get("content"), likeCondition);
                Predicate emailLike = criteriaBuilder.like(root.get("email"), likeCondition);
                //关键字存在author，content，或者email中皆可
                predicates.add(criteriaBuilder.or(authorLike, contentLike, emailLike));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();

        };
    }

}
