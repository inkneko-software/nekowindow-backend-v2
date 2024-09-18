package com.inkneko.nekowindow.api.oss.client;

import com.inkneko.nekowindow.api.oss.dto.GenUploadUrlDTO;
import com.inkneko.nekowindow.api.oss.vo.GenUploadUrlVO;
import com.inkneko.nekowindow.api.oss.vo.UploadRecordVO;
import com.inkneko.nekowindow.common.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("nekowindow-service-oss")
public interface OssFeignClient {

    @PostMapping("/internal/oss/genUploadUrl")
    Response<GenUploadUrlVO> genUploadUrl(GenUploadUrlDTO dto);

    @GetMapping("/internal/oss/isObjectExists")
    Response<UploadRecordVO> isObjectExists(@RequestParam String bucket, @RequestParam String objectKey);

}
