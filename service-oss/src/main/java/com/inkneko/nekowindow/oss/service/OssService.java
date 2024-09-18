package com.inkneko.nekowindow.oss.service;

import com.inkneko.nekowindow.oss.entity.UploadRecord;

public interface OssService {
    /**
     * 生成用于上传至指定位置的预签名链接
     *
     * @param bucket 桶
     * @param objectKey 对象key
     * @param uid 申请链接的用户id
     * @return 预签名连接
     * @exception com.inkneko.nekowindow.common.ServiceException 若生成失败（通常情况下为目标已存在），产生该异常
     */
    String generatePreSignedUrl(String bucket, String objectKey, Long uid);

    /**
     * 检查是否存在某个对象
     *
     * @param bucket 桶
     * @param objectKey 对象key
     * @return 若存在则返回上传记录，否则返回null
     */
    UploadRecord isObjectExists(String bucket, String objectKey);

}
