package top.panll.assist.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.panll.assist.controller.bean.WVPResult;
import top.panll.assist.dto.MinioConfigDTO;
import top.panll.assist.dto.MinioFileDTO;
import top.panll.assist.enums.ErrorMsgEnum;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @des: 应用-摄像头-日期-文件
 * @author: Yingkb
 * @create: 2022/03/10 09:01
 **/
public class MinioFileUtil {

    private final static Logger logger = LoggerFactory.getLogger(MinioFileUtil.class);

    /**
     * 服务器IP
     */
    private static String endPoint;
    /**
     * 端口
     */
    private static Integer endPointPort;
    /**
     * 默认key
     */
    private static String defaultAccessKey;
    /**
     * 默认密码
     */
    private static String defaultSecretKey;
    /**
     * 默认桶
     */
    private static String defaultBucket;

    /**
     * 默认过期时间
     */
    private static final Integer ONE_YEAR_EXPIRY = 7;

    public static final String FILE_SEPARATOR = "/";

    private static MinioClient minioClient;

    public static void initDefault(MinioConfigDTO minioConfig) {
        endPoint = minioConfig.getEndPoint();
        endPointPort = minioConfig.getEndPointPort();
        defaultAccessKey = minioConfig.getDefaultAccessKey();
        defaultSecretKey = minioConfig.getDefaultSecretKey();
        defaultBucket = minioConfig.getDefaultBucket();
        minioClient = MinioClient.builder()
                .endpoint(endPoint, endPointPort, false)
                .credentials(defaultAccessKey, defaultSecretKey)
                .build();
        logger.info("初始化上传工具类默认数据:{}", JSON.toJSONString(minioConfig));
    }


    /**
     * 检查Bucket是否存在
     *
     * @param bucket
     * @return
     */
    public static WVPResult<String> checkBucketExists(String bucket) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            return found ? WVPResult.buildErrorResult(ErrorMsgEnum.MINIO_BUCKET_EXIST) : WVPResult.buildSuccessResult();
        } catch (Exception e) {
            logger.error("检查Bucket是否存在出错:{}", e.getMessage());
        }
        return WVPResult.buildErrorResult(ErrorMsgEnum.FAILURE);
    }

    /**
     * 创建Bucket
     *
     * @param bucket
     * @return
     */
    public static WVPResult<String> makeBucket(String bucket) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            return WVPResult.buildSuccessResult();
        } catch (Exception e) {
            logger.error("创建Bucket出错:{}", e.getMessage());
        }
        return WVPResult.buildErrorResult(ErrorMsgEnum.FAILURE);
    }

    /**
     * 删除Bucket
     *
     * @param bucket
     * @return
     */
    public static WVPResult<String> removeBucket(String bucket) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());
            return WVPResult.buildSuccessResult();
        } catch (Exception e) {
            logger.error("删除Bucket出错:{}", e.getMessage());
        }
        return WVPResult.buildErrorResult(ErrorMsgEnum.FAILURE);
    }

    /**
     * 上传文件
     *
     * @param minioFileDTO
     * @return
     */
    public static WVPResult<String> uploadFile(MinioFileDTO minioFileDTO) {
        logger.info("上传文件入参:{}",minioFileDTO.toString());
        String bucket = minioFileDTO.getBucket();
        if (StrUtil.isBlank(bucket)) {
            bucket = defaultBucket;
        }
        WVPResult<String> wvpResult = checkBucketExists(bucket);
        if (wvpResult.isSuccess()) {
            wvpResult = makeBucket(bucket);
            if (!wvpResult.isSuccess()) {
                return WVPResult.buildErrorResult(ErrorMsgEnum.MINIO_BUCKET_FAIL);
            }
        }
        String filePath = minioFileDTO.getFilePath();
        File file = new File(filePath);
        if (!FileUtil.isFile(file)) {
            return WVPResult.buildErrorResult(ErrorMsgEnum.FILE_IS_EXIST);
        }
        try {
            String root = minioFileDTO.getFileRootName();
            String object = minioFileDTO.getFileName();
            if (!StrUtil.isBlank(root)) {
                object = root + FILE_SEPARATOR + object;
            }
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .filename(filePath)
                    .build();
            logger.info("uploadObjectArgs:{}",uploadObjectArgs.toString());
            minioClient.uploadObject(uploadObjectArgs);
            FileUtil.del(file);
            return WVPResult.buildSuccessResult();
        } catch (Exception e) {
            logger.error("上传文件出错:{}", e.getMessage());
        }
        return WVPResult.buildErrorResult(ErrorMsgEnum.FAILURE);
    }

    /**
     * 删除文件
     *
     * @param minioFileDTO
     * @return
     */
    public static WVPResult<String> removeObj(MinioFileDTO minioFileDTO) {
        String bucket = minioFileDTO.getBucket();
        if (StrUtil.isBlank(bucket)) {
            bucket = defaultBucket;
        }
        try {
            String root = minioFileDTO.getFileRootName();
            String object = minioFileDTO.getFileName();
            if (!StrUtil.isBlank(root)) {
                object = root + FILE_SEPARATOR + object;
            }
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .build();
            minioClient.removeObject(removeObjectArgs);
            return WVPResult.buildSuccessResult();
        } catch (Exception e) {
            logger.error("删除文件出错:{}", e.getMessage());
        }
        return WVPResult.buildErrorResult(ErrorMsgEnum.FAILURE);
    }

    /**
     * 获取文件地址
     *
     * @param minioFileDTO
     * @return
     */
    public static WVPResult<String> getObjectUrl(MinioFileDTO minioFileDTO) {
        String bucket = minioFileDTO.getBucket();
        if (StrUtil.isBlank(bucket)) {
            bucket = defaultBucket;
        }
        String root = minioFileDTO.getFileRootName();
        String object = minioFileDTO.getFileName();
        if (!StrUtil.isBlank(root)) {
            object = root + FILE_SEPARATOR + object;
        }
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .object(object)
                .method(Method.GET)
                .expiry(null == minioFileDTO.getExpiryDays() ?
                        ONE_YEAR_EXPIRY : minioFileDTO.getExpiryDays(), TimeUnit.DAYS)
                .build();
        try {
            String url = minioClient.getPresignedObjectUrl(args);
            return WVPResult.buildSuccessResult(url);
        } catch (Exception e) {
            logger.error("获取文件地址出错:{}", e.getMessage());
        }
        return WVPResult.buildErrorResult(ErrorMsgEnum.FAILURE);
    }


}
