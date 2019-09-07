package com.leyou.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/30
 * @描述
 */
@Service
public class UploadService {
    //支持的文件类型
    private static final List<String> suffixes= Arrays.asList("image/png", "image/jpeg", "image/bmp");

    public String upload(MultipartFile file) {
        /*图片的校验*/
        //文件类型
        String contentType = file.getContentType();
        if (!suffixes.contains(contentType)){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //文件内容
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image==null){//判断为空时
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        /*图片的保存*/
        File path = new File("E:\\nginx\\nginx-1.14.0\\html");
        if (!path.exists()){//不存在，创建
            path.mkdirs();
        }
        //保存
        //创建新的文件名
        String filename = file.getOriginalFilename();//原始文件名
        String newFileName = UUID.randomUUID().toString() + filename.substring(filename.lastIndexOf("."));
        try {
            file.transferTo(new File(path,newFileName));
            return "http://image.leyou.com/"+newFileName;
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
/*阿里云*/
    @Autowired
    private OSSProperties prop;
    @Autowired
    private OSS client;
    public Map<String, Object> getSignature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }
}
