package com.wjk.halo.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "JournalComment")
@DiscriminatorValue("2")
public class JournalComment extends BaseComment {
}
