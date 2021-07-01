package com.wjk.halo.event.logger;

import com.wjk.halo.model.enums.LogType;
import com.wjk.halo.model.params.LogParam;
import com.wjk.halo.model.params.LoginParam;
import org.springframework.context.ApplicationEvent;

public class LogEvent extends ApplicationEvent {

    private final LogParam logParam;

    public LogEvent(Object source, LogParam logParam) {
        super(source);
        this.logParam = logParam;
    }

    public LogEvent(Object source, String logKey, LogType logType, String content){
        this(source, new LogParam(logKey, logType, content));
    }

    public LogParam getLogParam(){
        return logParam;
    }


}
