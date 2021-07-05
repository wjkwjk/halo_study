package com.wjk.halo.model.entity;

import lombok.EqualsAndHashCode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "PostMeta")
@DiscriminatorValue("0")
@EqualsAndHashCode(callSuper = true)
public class PostMeta extends BaseMeta{
}
