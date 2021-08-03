package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.PhotoDTO;
import com.wjk.halo.model.entity.Photo;
import com.wjk.halo.model.params.PhotoParam;
import com.wjk.halo.model.params.PhotoQuery;
import com.wjk.halo.service.PhotoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping(value = "latest")
    public List<PhotoDTO> listPhotos(@SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort){
        return photoService.listDtos(sort);
    }

    @GetMapping
    public Page<PhotoDTO> pageBy(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC)Pageable pageable,
                                 PhotoQuery photoQuery){
        return photoService.pageDtosBy(pageable, photoQuery);
    }

    @GetMapping("{photoId:\\d+}")
    public PhotoDTO getBy(@PathVariable("photoId") Integer photoId){
        return new PhotoDTO().convertFrom(photoService.getById(photoId));
    }

    @DeleteMapping("{photoId:\\d+}")
    public void deletePermanently(@PathVariable("photoId") Integer photoId){
        photoService.removeById(photoId);
    }

    @PostMapping
    public PhotoDTO createBy(@Valid @RequestBody PhotoParam photoParam){
        return new PhotoDTO().convertFrom(photoService.createBy(photoParam));
    }

    @PutMapping("{photoId:\\d+}")
    public PhotoDTO updateBy(@PathVariable("photoId") Integer photoId,
                             @RequestBody @Valid PhotoParam photoParam){
        Photo photo = photoService.getById(photoId);

        photoParam.update(photo);

        return new PhotoDTO().convertFrom(photoService.update(photo));
    }

    @GetMapping("teams")
    public List<String> listTeams(){
        return photoService.listAllTeams();
    }

}
