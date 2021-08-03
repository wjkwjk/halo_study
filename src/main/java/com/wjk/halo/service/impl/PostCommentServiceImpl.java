package com.wjk.halo.service.impl;

import cn.hutool.core.date.DateUtil;
import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.entity.Option;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.entity.PostComment;
import com.wjk.halo.model.enums.PostPermalinkType;
import com.wjk.halo.model.vo.PostCommentWithPostVO;
import com.wjk.halo.repository.PostCommentRepository;
import com.wjk.halo.repository.PostRepository;
import com.wjk.halo.repository.base.BaseCommentRepository;
import com.wjk.halo.service.CommentBlackListService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostCommentService;
import com.wjk.halo.service.UserService;
import com.wjk.halo.utils.ServiceUtils;
import javafx.geometry.Pos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;

@Slf4j
@Service
public class PostCommentServiceImpl extends BaseCommentServiceImpl<PostComment> implements PostCommentService {

    private final PostRepository postRepository;
    private final CommentBlackListService commentBlackListService;

    public PostCommentServiceImpl(PostCommentRepository postCommentRepository,
                                  PostRepository postRepository,
                                  UserService userService,
                                  OptionService optionService,
                                  CommentBlackListService commentBlackListService,
                                  ApplicationEventPublisher eventPublisher) {
        super(postCommentRepository, optionService, userService, eventPublisher);
        this.postRepository = postRepository;
        this.commentBlackListService = commentBlackListService;
    }


    @Override
    public Page<PostCommentWithPostVO> convertToWithPostVo(Page<PostComment> commentPage) {
        return new PageImpl<>(convertToWithPostVo(commentPage.getContent()), commentPage.getPageable(), commentPage.getTotalElements());
    }

    @Override
    public List<PostCommentWithPostVO> convertToWithPostVo(List<PostComment> postComments) {
        if (CollectionUtils.isEmpty(postComments)){
            return Collections.emptyList();
        }

        Set<Integer> postIds = ServiceUtils.fetchProperty(postComments, PostComment::getPostId);

        Map<Integer, Post> postMap = ServiceUtils.convertToMap(postRepository.findAllById(postIds), Post::getId);

        return postComments.stream()
                .filter(comment -> postMap.containsKey(comment.getPostId()))
                .map(comment -> {
                    PostCommentWithPostVO postCommentWithPostVO = new PostCommentWithPostVO().convertFrom(comment);

                    BasePostMinimalDTO basePostMinimalDTO = new BasePostMinimalDTO().convertFrom(postMap.get(comment.getPostId()));

                    postCommentWithPostVO.setPost(buildPostFullPath(basePostMinimalDTO));

                    return postCommentWithPostVO;

                }).collect(Collectors.toList());

    }

    @Override
    public PostCommentWithPostVO convertToWithPostVo(PostComment comment) {
        PostCommentWithPostVO postCommentWithPostVO = new PostCommentWithPostVO().convertFrom(comment);

        BasePostMinimalDTO basePostMinimalDTO = new BasePostMinimalDTO().convertFrom(postRepository.getOne(comment.getPostId()));

        postCommentWithPostVO.setPost(buildPostFullPath(basePostMinimalDTO));
        return postCommentWithPostVO;
    }

    private BasePostMinimalDTO buildPostFullPath(BasePostMinimalDTO post){
        PostPermalinkType permalinkType = optionService.getPostPermalinkType();

        String pathSuffix = optionService.getPathSuffix();

        String archivesPrefix = optionService.getArchivesPrefix();

        int month = DateUtil.month(post.getCreateTime()) + 1;

        String monthString = month < 10 ? "0" + month : String.valueOf(month);

        int day = DateUtil.dayOfMonth(post.getCreateTime());

        String dayString = day < 10 ? "0" + day : String.valueOf(day);

        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()){
            fullPath.append(optionService.getBlogBaseUrl());
        }
        fullPath.append(URL_SEPARATOR);

        if (permalinkType.equals(PostPermalinkType.DEFAULT)){
            fullPath.append(archivesPrefix)
                    .append(URL_SEPARATOR)
                    .append(post.getSlug())
                    .append(pathSuffix);
        }else if (permalinkType.equals(PostPermalinkType.ID)) {
            fullPath.append("?p=")
                    .append(post.getId());
        } else if (permalinkType.equals(PostPermalinkType.DATE)) {
            fullPath.append(DateUtil.year(post.getCreateTime()))
                    .append(URL_SEPARATOR)
                    .append(monthString)
                    .append(URL_SEPARATOR)
                    .append(post.getSlug())
                    .append(pathSuffix);
        } else if (permalinkType.equals(PostPermalinkType.DAY)) {
            fullPath.append(DateUtil.year(post.getCreateTime()))
                    .append(URL_SEPARATOR)
                    .append(monthString)
                    .append(URL_SEPARATOR)
                    .append(dayString)
                    .append(URL_SEPARATOR)
                    .append(post.getSlug())
                    .append(pathSuffix);
        } else if (permalinkType.equals(PostPermalinkType.YEAR)) {
            fullPath.append(DateUtil.year(post.getCreateTime()))
                    .append(URL_SEPARATOR)
                    .append(post.getSlug())
                    .append(pathSuffix);
        }

        post.setFullPath(fullPath.toString());

        return post;

    }

}
