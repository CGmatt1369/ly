package com.leyou.controller;

import com.leyou.service.impl.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/30
 * @描述
 */
@RestController
public class UploadController {
    @Autowired
    private UploadService uploadService;

    /**
     * 上传功能,上传并返回路径,本地
     */
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam(name = "file")MultipartFile file){
        return ResponseEntity.ok(this.uploadService.upload(file));
    }

    /**
     * 上传图片到阿里云
     */
    @GetMapping("signature")
    public ResponseEntity<Map<String,Object>> getAliSignature(){
        return ResponseEntity.ok(uploadService.getSignature());
    }
}
