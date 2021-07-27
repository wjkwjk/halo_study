package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.Link;
import com.wjk.halo.repository.LinkRepository;
import com.wjk.halo.service.LinkService;
import com.wjk.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class LinkServiceImpl extends AbstractCrudService<Link, Integer> implements LinkService {

    private final LinkRepository linkRepository;

    public LinkServiceImpl(LinkRepository linkRepository){
        super(linkRepository);
        this.linkRepository = linkRepository;
    }

}
