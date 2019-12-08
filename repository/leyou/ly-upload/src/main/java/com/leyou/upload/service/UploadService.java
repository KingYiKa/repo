package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author wangshiyu
 */
@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties prop;

//    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/png","image/bmp","image/jpg");

    public String uploadImage(MultipartFile file) {

        try {
            //文件校验
            String contentType = file.getContentType();

            if (!prop.getAllowTypes().contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_FIE_TYPE);
            }
            //文件校验内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null){
                throw new LyException(ExceptionEnum.INVALID_FIE_TYPE);
            }

            //准备目标路径
            //File dest = new File("D:\\qr",file.getOriginalFilename());
            //保存文件到本地
            //file.transferTo(dest);

            //上传FastDFS
//            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);

            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

            //返回路径
            return prop.getBaseUrl() + storePath.getFullPath();
        } catch (Exception e) {
            //上传失败
            log.error("[文件上传]   文件上传失败！",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }

    }
}
