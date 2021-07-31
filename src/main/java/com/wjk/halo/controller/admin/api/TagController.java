package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.TagDTO;
import com.wjk.halo.model.entity.Tag;
import com.wjk.halo.model.params.TagParam;
import com.wjk.halo.service.PostTagService;
import com.wjk.halo.service.TagService;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/tags")
public class TagController {

    private final TagService tagService;

    private final PostTagService postTagService;

    public TagController(TagService tagService, PostTagService postTagService) {
        this.tagService = tagService;
        this.postTagService = postTagService;
    }

    @GetMapping
    public List<? extends TagDTO> listTags(@SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                                           @ApiParam("Return more information(post count) if it is set")
                                           @RequestParam(name = "more", required = false, defaultValue = "false") Boolean more){
        if (more){
            return postTagService.listTagWithCountDtos(sort);
        }
        return tagService.convertTo(tagService.listAll(sort));
    }

    @PostMapping
    public TagDTO createTag(@Valid @RequestBody TagParam tagParam){
        Tag tag = tagParam.convertTo();
        log.debug("Tag to be created: [{}]", tag);

        return tagService.convertTo(tagService.create(tag));

    }

    @GetMapping("{tagId:\\d+}")
    public TagDTO getBy(@PathVariable("tagId") Integer tagId){
        return tagService.convertTo(tagService.getById(tagId));
    }

    @PutMapping("{tagId:\\d+}")
    public TagDTO updateBy(@PathVariable("tagId") Integer tagId,
                           @Valid @RequestBody TagParam tagParam){
        Tag tag = tagService.getById(tagId);

        tagParam.update(tag);

        return tagService.convertTo(tagService.update(tag));
    }

    @DeleteMapping("{tagId:\\d+}")
    public TagDTO deletePermanently(@PathVariable("tagId") Integer tagId){
        Tag deletedTag = tagService.removeById(tagId);

        postTagService.removeByTagId(tagId);

        return tagService.convertTo(deletedTag);
    }

}
