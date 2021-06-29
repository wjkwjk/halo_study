package com.wjk.halo.cache;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CacheWrapper<V> implements Serializable {
    private V data;
    private Date expireAt;
    private Date createAt;
}
