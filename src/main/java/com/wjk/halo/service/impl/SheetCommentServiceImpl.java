package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.SheetComment;
import com.wjk.halo.repository.SheetCommentRepository;
import com.wjk.halo.repository.SheetRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.SheetCommentService;
import com.wjk.halo.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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


}
