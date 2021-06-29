package com.wjk.halo.cache;

import java.io.Serializable;
import java.util.Date;

public class CacheWrapper<V> implements Serializable {
    private V data;
    private Date expireAt;
    private Date createAt;
}
