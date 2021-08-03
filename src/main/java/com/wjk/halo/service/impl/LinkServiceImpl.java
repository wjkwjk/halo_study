package com.wjk.halo.service.impl;

import com.wjk.halo.exception.AlreadyExistsException;
import com.wjk.halo.model.dto.LinkDTO;
import com.wjk.halo.model.entity.Link;
import com.wjk.halo.model.params.LinkParam;
import com.wjk.halo.repository.LinkRepository;
import com.wjk.halo.service.LinkService;
import com.wjk.halo.service.base.AbstractCrudService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LinkServiceImpl extends AbstractCrudService<Link, Integer> implements LinkService {

    private final LinkRepository linkRepository;

    public LinkServiceImpl(LinkRepository linkRepository){
        super(linkRepository);
        this.linkRepository = linkRepository;
    }

    @Override
    public @NotNull List<LinkDTO> listDtos(@NotNull Sort sort) {
        return convertTo(listAll(sort));
    }

    @Override
    public Link createBy(LinkParam linkParam) {
        boolean exist = existByName(linkParam.getName());

        if (exist){
            throw new AlreadyExistsException("友情链接 " + linkParam.getName() + " 已存在").setErrorData(linkParam.getName());
        }

        exist = existByUrl(linkParam.getUrl());

        if (exist){
            throw new AlreadyExistsException("友情链接 " + linkParam.getUrl() + " 已存在").setErrorData(linkParam.getUrl());
        }
        return create(linkParam.convertTo());
    }

    @Override
    public boolean existByName(String name) {
        Link link = new Link();
        link.setName(name);
        return linkRepository.exists(Example.of(link));
    }

    @Override
    public boolean existByUrl(String url) {
        Link link = new Link();
        link.setUrl(url);

        return linkRepository.exists(Example.of(link));
    }

    @Override
    public @NotNull Link updateBy(Integer id, @NotNull LinkParam linkParam) {
        boolean exist = linkRepository.existsByNameAndIdNot(linkParam.getName(), id);
        if (exist){
            throw new AlreadyExistsException("友情链接 " + linkParam.getName() + " 已存在").setErrorData(linkParam.getName());
        }

        exist = linkRepository.existsByUrlAndIdNot(linkParam.getUrl(), id);
        if (exist) {
            throw new AlreadyExistsException("友情链接 " + linkParam.getUrl() + " 已存在").setErrorData(linkParam.getUrl());
        }

        Link link = getById(id);
        linkParam.update(link);

        return update(link);

    }

    @Override
    public @NotNull List<Link> listAllByRandom() {
        List<Link> allLink = linkRepository.findAll();
        Collections.shuffle(allLink);
        return allLink;
    }

    @Override
    public List<String> listAllTeams() {
        return linkRepository.findAllTeams();
    }


    @NonNull
    private List<LinkDTO> convertTo(@Nullable List<Link> links){
        if (CollectionUtils.isEmpty(links)){
            return Collections.emptyList();
        }
        return links.stream().map(link -> (LinkDTO) new LinkDTO().convertFrom(link))
                .collect(Collectors.toList());
    }

}
