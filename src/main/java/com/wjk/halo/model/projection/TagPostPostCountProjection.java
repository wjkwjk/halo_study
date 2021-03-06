package com.wjk.halo.model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagPostPostCountProjection {

    private Long postCount;

    private Integer tagId;

}
