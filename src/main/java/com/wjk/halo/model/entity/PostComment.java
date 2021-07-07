package com.wjk.halo.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "PostComment")
@DiscriminatorValue("0")
public class PostComment extends BaseComment{
}
