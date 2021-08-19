package com.wjk.halo.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.wjk.halo.model.support.CommentPage;
import org.springframework.data.domain.Page;

import java.io.IOException;

/**
 * 自定义Page对象的序列化方法
 */

public class PageJacksonSerializer extends JsonSerializer<Page> {

    @Override
    public void serialize(Page page, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeStartObject();

        generator.writeObjectField("content", page.getContent());
        generator.writeNumberField("pages", page.getTotalPages());
        generator.writeNumberField("total", page.getTotalElements());
        generator.writeNumberField("page", page.getNumber());
        generator.writeNumberField("rpp", page.getSize());
        generator.writeBooleanField("hasNext", page.hasNext());
        generator.writeBooleanField("hasPrevious", page.hasPrevious());
        generator.writeBooleanField("isFirst", page.isFirst());
        generator.writeBooleanField("isLast", page.isLast());
        generator.writeBooleanField("isEmpty", page.isEmpty());
        generator.writeBooleanField("hasContent", page.hasContent());

        // Handle comment page
        if (page instanceof CommentPage) {
            CommentPage commentPage = (CommentPage) page;
            generator.writeNumberField("commentCount", commentPage.getCommentCount());
        }

        generator.writeEndObject();
    }
}
