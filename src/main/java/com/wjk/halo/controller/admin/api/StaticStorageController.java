package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.params.StaticContentParam;
import com.wjk.halo.model.support.StaticFile;
import com.wjk.halo.service.StaticStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/statics")
public class StaticStorageController {

    private final StaticStorageService staticStorageService;

    public StaticStorageController(StaticStorageService staticStorageService) {
        this.staticStorageService = staticStorageService;
    }

    @GetMapping
    public List<StaticFile> list(){
        return staticStorageService.listStaticFolder();
    }

    @DeleteMapping
    public void deletePermanently(@RequestParam("path") String path){
        staticStorageService.delete(path);
    }

    @PostMapping
    public void createFolder(String basePath,
                             @RequestParam("folderName") String folderName){
        staticStorageService.createFolder(basePath, folderName);
    }

    @PostMapping("upload")
    public void upload(String basePath,
                       @RequestPart("file") MultipartFile file){
        staticStorageService.upload(basePath, file);
    }

    @PostMapping("rename")
    public void rename(String basePath, String newName){
        staticStorageService.rename(basePath, newName);
    }

    @PutMapping("files")
    public void save(@RequestBody StaticContentParam param){
        staticStorageService.save(param.getPath(), param.getContent());
    }

}
