package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.Photo;
import lombok.Data;

import java.util.Date;

@Data
public class PhotoDTO implements OutputConverter<PhotoDTO, Photo> {
    private Integer id;

    private String name;

    private String thumbnail;

    private Date takeTime;

    private String url;

    private String team;

    private String location;

    private String description;
}
