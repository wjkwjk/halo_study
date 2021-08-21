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

    /**
     * 以树的结构返回静态文件
     * @return
     */
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

    /**
     * MultipartFile：Spring框架中的MultipartFile来处理文件
     * 一种可以接收使用多种请求方式来进行上传文件的代表形式。
     * 也就是说，如果你想用spring框架来实现项目中的文件上传功能，则MultipartFile可能是最合适的选择，
     * 而这里提到的多种请求方式则可以通俗理解为以表单的形式提交
     * @param basePath
     * @param file  前端提交的file,后端使用MultipartFile接收
     */
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
