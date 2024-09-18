package com.inkneko.nekowindow.oss.feign;

import com.inkneko.nekowindow.api.oss.client.OssFeignClient;
import com.inkneko.nekowindow.api.oss.dto.GenUploadUrlDTO;
import com.inkneko.nekowindow.api.oss.vo.GenUploadUrlVO;
import com.inkneko.nekowindow.api.oss.vo.UploadRecordVO;
import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.oss.entity.UploadRecord;
import com.inkneko.nekowindow.oss.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class OssInternalController {

    OssService ossService;

    public OssInternalController(OssService ossService) {
        this.ossService = ossService;
    }

    @PostMapping("/internal/oss/genUploadUrl")
    @Operation(summary = "获取上传链接")
    public Response<GenUploadUrlVO> genUploadUrl(@RequestBody GenUploadUrlDTO dto, HttpServletRequest request) {
        Long uid =  GatewayAuthUtils.auth(request);
        String url = ossService.generatePreSignedUrl(dto.getBucket(), dto.getObjectKey(), uid);
        return new Response<>("ok", new GenUploadUrlVO(url));
    }


    @GetMapping("/internal/oss/isObjectExists")
    @Operation(summary = "查询对象是否存在")
    public Response<UploadRecordVO> isObjectExists(@RequestParam String bucket, @RequestParam String objectKey){
        UploadRecord uploadRecord = ossService.isObjectExists(bucket, objectKey);
        if (uploadRecord == null){
            return new Response<>(404, "指定对象不存在");
        }
        return new Response<>("ok", new UploadRecordVO(uploadRecord.getOssId(), uploadRecord.getUid(), uploadRecord.getEndpoint(), uploadRecord.getBucket(), uploadRecord.getObjectKey()));
    }
}
