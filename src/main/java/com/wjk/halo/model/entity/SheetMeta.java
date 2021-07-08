package com.wjk.halo.model.entity;

import lombok.EqualsAndHashCode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "SheetMeta")
@DiscriminatorValue("1")
@EqualsAndHashCode(callSuper = true)
public class SheetMeta extends BaseMeta{
}
