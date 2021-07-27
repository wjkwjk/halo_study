package com.wjk.halo.handler.file;

import com.wjk.halo.exception.RepeatTypeException;
import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.model.enums.AttachmentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FileHandlers {

    private final ConcurrentHashMap<AttachmentType, FileHandler> fileHandlers = new ConcurrentHashMap<>(16);

    public FileHandlers(ApplicationContext applicationContext) {
        addFileHandlers(applicationContext.getBeansOfType(FileHandler.class).values());
        log.info("Registered {} file handler(s)", fileHandlers.size());
    }

    @NonNull
    public FileHandlers addFileHandlers(@Nullable Collection<FileHandler> fileHandlers){
        if (!CollectionUtils.isEmpty(fileHandlers)){
            for (FileHandler handler : fileHandlers){
                if (this.fileHandlers.containsKey(handler.getAttachmetType())){
                    throw new RepeatTypeException("Same attachment type implements must be unique");
                }
                this.fileHandlers.put(handler.getAttachmetType(), handler);
            }
        }
        return this;
    }
}
