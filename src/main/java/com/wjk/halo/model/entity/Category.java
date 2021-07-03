package com.wjk.halo.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "categories_name", columnList = "name"),
        @Index(name = "categories_parent_id", columnList = "parent_id")})
@ToString
@EqualsAndHashCode
public class Category extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "com.wjk.halo.model.entity.support.CustomIdGenerator")
    private Integer id;


    @Column(name = "name", nullable = false)
    private String name;


    @Deprecated
    @Column(name = "slug_name")
    private String slugName;


    @Column(name = "slug", unique = true)
    private String slug;


    @Column(name = "description", length = 100)
    private String description;


    @Column(name = "thumbnail", length = 1023)
    private String thumbnail;


    @Column(name = "parent_id")
    @ColumnDefault("0")
    private Integer parentId;

    @Override
    public void prePersist() {
        super.prePersist();

        if (description == null) {
            description = "";
        }

        if (parentId == null || parentId < 0) {
            parentId = 0;
        }
    }
}
