package com.inkneko.nekowindow.oss.feign;

import com.inkneko.nekowindow.api.oss.client.OssFeignClient;
import com.inkneko.nekowindow.api.oss.dto.GenUploadUrlDTO;
import com.inkneko.nekowindow.api.oss.vo.GenUploadUrlVO;
import com.inkneko.nekowindow.api.oss.vo.UploadRecordVO;
import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.oss.config.S3Config;
import com.inkneko.nekowindow.oss.entity.UploadRecord;
import com.inkneko.nekowindow.oss.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class OssInternalController {

    OssService ossService;
    S3Config s3Config;

    public OssInternalController(OssService ossService, S3Config s3Config) {
        this.ossService = ossService;
        this.s3Config = s3Config;
    }

    @PostMapping("/internal/oss/genUploadUrl")
    @Operation(summary = "获取上传链接")
    public Response<GenUploadUrlVO> genUploadUrl(@RequestBody GenUploadUrlDTO dto, HttpServletRequest request) {
        Long uid = GatewayAuthUtils.auth(request);
        String url = ossService.generatePreSignedUrl(dto.getBucket(), dto.getObjectKey(), uid);
        return new Response<>("ok", new GenUploadUrlVO(url));
    }


    @GetMapping("/internal/oss/isObjectExists")
    @Operation(summary = "查询对象是否存在")
    public Response<UploadRecordVO> isObjectExists(@RequestParam String bucket, @RequestParam String objectKey) {
        UploadRecord uploadRecord = ossService.isObjectExists(bucket, objectKey);
        if (uploadRecord == null) {
            return new Response<>(404, "指定对象不存在");
        }
        return new Response<>("ok", new UploadRecordVO(uploadRecord.getOssId(), uploadRecord.getUid(), uploadRecord.getEndpoint(), uploadRecord.getBucket(), uploadRecord.getObjectKey()));
    }

    @GetMapping("/internal/oss/isURLValid")
    @Operation(summary = "判断指定链接是否合法", description = "判断是否为当前站点内资源，且对象存在")
    public Response<UploadRecordVO> isURLValid(@RequestParam String url) {
        Pattern pattern = Pattern.compile(s3Config.getEndpoint() + "/(.+?)/(.+?)");
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            return new Response<>(404, "指定链接非站内资源");
        }
        String bucket = matcher.group(1);
        String objectKey = matcher.group(2);
        UploadRecord uploadRecord = ossService.isObjectExists(bucket, objectKey);
        if (uploadRecord == null) {
            return new Response<>(404, "指定对象不存在");
        }
        return new Response<>("ok", new UploadRecordVO(uploadRecord.getOssId(), uploadRecord.getUid(), uploadRecord.getEndpoint(), uploadRecord.getBucket(), uploadRecord.getObjectKey()));
    }
}
