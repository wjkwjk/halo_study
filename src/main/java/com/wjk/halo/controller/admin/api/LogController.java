package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.LogDTO;
import com.wjk.halo.model.entity.Link;
import com.wjk.halo.model.entity.Log;
import com.wjk.halo.service.LogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("latest")
    public List<LogDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top){
        return logService.pageLatest(top).getContent();
    }

    @GetMapping
    public Page<LogDTO> pageBy(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable){
        Page<Log> logPage = logService.listAll(pageable);
        return logPage.map(log -> new LogDTO().convertFrom(log));
    }

    @GetMapping("clear")
    public void clear(){
        logService.removeAll();
    }

}
