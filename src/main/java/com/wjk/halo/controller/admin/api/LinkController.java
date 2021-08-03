package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.LinkDTO;
import com.wjk.halo.model.entity.Link;
import com.wjk.halo.model.params.LinkParam;
import com.wjk.halo.service.LinkService;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理友情连接
 */
@RestController
@RequestMapping("/api/admin/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public List<LinkDTO> listLinks(@SortDefault(sort = "team", direction = Sort.Direction.DESC) Sort sort){
        return linkService.listDtos(sort.and(Sort.by(Sort.Direction.ASC, "priority")));
    }

    @GetMapping("{id:\\d+}")
    public LinkDTO getBy(@PathVariable("id") Integer id){
        return new LinkDTO().convertFrom(linkService.getById(id));
    }

    @PostMapping
    public LinkDTO createBy(@RequestBody @Valid LinkParam linkParam){
        Link link = linkService.createBy(linkParam);
        return new LinkDTO().convertFrom(link);
    }

    @PutMapping("{id:\\d+}")
    public LinkDTO updateBy(@PathVariable("id") Integer id,
                            @RequestBody @Valid LinkParam linkParam){
        Link link = linkService.updateBy(id, linkParam);
        return new LinkDTO().convertFrom(link);
    }

    @DeleteMapping("{id:\\d+}")
    public void deletePermanently(@PathVariable("id") Integer id){
        linkService.removeById(id);
    }

    public List<String> teams(){
        return linkService.listAllTeams();
    }

}
