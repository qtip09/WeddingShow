package vip.maosi.weddingServer.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vip.maosi.weddingServer.config.ServerConfig;
import vip.maosi.weddingServer.config.WeddingShowConfig;
import vip.maosi.weddingServer.response.RGenerator;
import vip.maosi.weddingServer.response.ResEntity;
import vip.maosi.weddingServer.service.FileService;
import vip.maosi.weddingServer.util.StringUtils;
import vip.maosi.weddingServer.util.file.FileUploadUtils;
import vip.maosi.weddingServer.util.file.FileUtils;
import vip.maosi.weddingServer.util.file.MimeTypeUtils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author liudw
 * @Date 2025/4/25 14:19
 * @Version v1.0
 */
@Validated
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    private WeddingShowConfig weddingShowConfig;
    @Autowired
    private FileService fileService;
    /**
     * 通用上传请求
     */
    @PostMapping("/common/upload")
    @ResponseBody
    public ResEntity uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = WeddingShowConfig.getUploadPath();
            String realName = file.getOriginalFilename();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            String type = file.getContentType();
            Integer fileId = fileService.saveFile(fileName,realName,type);
            if (fileId > 0){
                LinkedHashMap map = new LinkedHashMap();
                map.put("fileId",fileId);
                map.put("realName",realName);
                map.put("fileName", fileName);
                map.put("url", url);
                return RGenerator.resSuccess(map);
            }else {
                return RGenerator.resFail("文件入库异常");
            }
        }
        catch (Exception e)
        {
            return RGenerator.resFail(file.getOriginalFilename()+" 上传失败:"+ (e.getMessage().contains("allowed extension")
                    ?"不支持该文件类型":
                    e.getMessage()));
        }
    }

    @GetMapping("common/download")
    public void fileDownload(Integer fileId , HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            vip.maosi.weddingServer.domain.File sfile = fileService.queryById(fileId);
            String realFileName = sfile.getFileName();
            String filePath = sfile.getFilePath();
            filePath = filePath.replace("/profile",WeddingShowConfig.getProfile());
            File file = new File(filePath);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Length",String.valueOf(file.length()));
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    @GetMapping("/fetch/{fileId:.+}")
    public ResponseEntity<Resource> fetch(@PathVariable Integer fileId) {
        try
        {
            vip.maosi.weddingServer.domain.File sfile = fileService.queryById(fileId);
            String realFileName = sfile.getFileName();
            String filePath = sfile.getFilePath();
            filePath = filePath.replace("/profile",WeddingShowConfig.getProfile());
            File file = new File(filePath);
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(sfile.getType())).body(resource);
        }
        catch (Exception e)
        {

            log.error("下载文件失败", e);
            return ResponseEntity.notFound().build();
        }

    }

}
