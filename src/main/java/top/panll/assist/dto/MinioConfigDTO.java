package top.panll.assist.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @des:
 * @author: Yingkb
 * @create: 2022/03/10 09:15
 **/
@Component
public class MinioConfigDTO {

    /**
     * 服务器IP
     */
    @Value("${minio.end_point}")
    private String endPoint;
    /**
     * 端口
     */
    @Value("${minio.end_point_port}")
    private Integer endPointPort;
    /**
     * 默认key
     */
    @Value("${minio.access_key}")
    private String defaultAccessKey;
    /**
     * 默认密码
     */
    @Value("${minio.secret_key}")
    private String defaultSecretKey;
    /**
     * 默认桶
     */
    @Value("${minio.bucket}")
    private String defaultBucket;

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public Integer getEndPointPort() {
        return endPointPort;
    }

    public void setEndPointPort(Integer endPointPort) {
        this.endPointPort = endPointPort;
    }

    public String getDefaultAccessKey() {
        return defaultAccessKey;
    }

    public void setDefaultAccessKey(String defaultAccessKey) {
        this.defaultAccessKey = defaultAccessKey;
    }

    public String getDefaultSecretKey() {
        return defaultSecretKey;
    }

    public void setDefaultSecretKey(String defaultSecretKey) {
        this.defaultSecretKey = defaultSecretKey;
    }

    public String getDefaultBucket() {
        return defaultBucket;
    }

    public void setDefaultBucket(String defaultBucket) {
        this.defaultBucket = defaultBucket;
    }
}
