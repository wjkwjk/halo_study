package com.wjk.halo.handler.theme.config.support;

import com.wjk.halo.model.enums.DataType;
import com.wjk.halo.model.enums.InputType;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class Item {

    /**
     * Item name.
     */
    private String name;

    /**
     * Item label.
     */
    private String label;

    /**
     * Item input type, default is text.
     */
    private InputType type;

    /**
     * Item data type, default is string.
     */
    private DataType dataType;

    /**
     * Item default value.
     */
    private Object defaultValue;

    /**
     * Text item placeholder.
     */
    private String placeholder;

    /**
     * Text item description.
     */
    private String description;

    /**
     * Item's options, default is empty list
     */
    private List<Option> options;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        return name.equals(item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

