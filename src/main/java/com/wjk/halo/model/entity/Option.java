package com.wjk.halo.model.entity;

import com.wjk.halo.model.enums.OptionType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;


@Data
@Entity
@Table(name = "options")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Option extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "com.wjk.halo.model.entity.support.CustomIdGenerator")
    private Integer id;

    @Column(name = "type")
    @ColumnDefault("0")
    private OptionType type;

    @Column(name = "option_key", length = 100, nullable = false)
    private String key;

    @Column(name = "option_value", nullable = false)
    @Lob
    private String value;

    public Option(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Option(OptionType type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }
}
