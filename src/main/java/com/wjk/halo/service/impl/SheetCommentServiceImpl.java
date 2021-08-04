package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.entity.SheetComment;
import com.wjk.halo.model.vo.SheetCommentWithSheetVO;
import com.wjk.halo.repository.SheetCommentRepository;
import com.wjk.halo.repository.SheetRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.SheetCommentService;
import com.wjk.halo.service.UserService;
import com.wjk.halo.utils.ServiceUtils;
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

@Service
public class SheetCommentServiceImpl extends BaseCommentServiceImpl<SheetComment> implements SheetCommentService {

    private final SheetRepository sheetRepository;

    public SheetCommentServiceImpl(SheetCommentRepository sheetCommentRepository,
                                   OptionService optionService,
                                   UserService userService,
                                   ApplicationEventPublisher eventPublisher,
                                   SheetRepository sheetRepository){
        super(sheetCommentRepository, optionService, userService, eventPublisher);
        this.sheetRepository = sheetRepository;
    }


    @Override
    public Page<SheetCommentWithSheetVO> convertToWithSheetVo(Page<SheetComment> sheetCommentPage) {
        return new PageImpl<>(convertToWithSheetVo(sheetCommentPage.getContent()), sheetCommentPage.getPageable(), sheetCommentPage.getTotalElements());

    }

    @Override
    public List<SheetCommentWithSheetVO> convertToWithSheetVo(List<SheetComment> sheetComments) {
        if (CollectionUtils.isEmpty(sheetComments)){
            return Collections.emptyList();
        }

        Set<Integer> sheetIds = ServiceUtils.fetchProperty(sheetComments, SheetComment::getPostId);

        Map<Integer, Sheet> sheetMap = ServiceUtils.convertToMap(sheetRepository.findAllById(sheetIds), Sheet::getId);

        return sheetComments.stream()
                .filter(comment -> sheetMap.containsKey(comment.getPostId()))
                .map(comment -> {
                    SheetCommentWithSheetVO sheetCommentWithSheetVO = new SheetCommentWithSheetVO().convertFrom(comment);

                    BasePostMinimalDTO postMinimalDTO = new BasePostMinimalDTO().convertFrom(sheetMap.get(comment.getPostId()));

                    sheetCommentWithSheetVO.setSheet(buildSheetFullPath(postMinimalDTO));
                    return sheetCommentWithSheetVO;
                }).collect(Collectors.toList());
    }

    @Override
    public SheetCommentWithSheetVO convertToWithSheetVo(SheetComment comment) {
        SheetCommentWithSheetVO sheetCommentWithSheetVO = new SheetCommentWithSheetVO().convertFrom(comment);
        BasePostMinimalDTO basePostMinimalDTO = new BasePostMinimalDTO().convertFrom(sheetRepository.getOne(comment.getPostId()));
        sheetCommentWithSheetVO.setSheet(buildSheetFullPath(basePostMinimalDTO));
        return sheetCommentWithSheetVO;
    }

    private BasePostMinimalDTO buildSheetFullPath(BasePostMinimalDTO basePostMinimalDTO){
        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()){
            fullPath.append(optionService.getBlogBaseUrl());
        }

        fullPath.append(URL_SEPARATOR)
                .append(optionService.getSheetPrefix())
                .append(URL_SEPARATOR)
                .append(basePostMinimalDTO.getSlug())
                .append(optionService.getPathSuffix());

        basePostMinimalDTO.setFullPath(fullPath.toString());
        return basePostMinimalDTO;
    }

}
