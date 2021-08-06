package com.wjk.halo.handler.theme.config.support;

import lombok.Data;

import java.util.List;

@Data
public class Group {

    /**
     * Tab name.
     */
    private String name;

    /**
     * Tab label.
     */
    private String label;

    /**
     * Tab's items, default is empty list.
     */
    private List<Item> items;
}
