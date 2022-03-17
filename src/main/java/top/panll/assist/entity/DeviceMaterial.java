package top.panll.assist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceMaterial {
    /**
     * 主键Id
     */
    private Integer id;

    /**
     * 设备Id
     */
    private String deviceId;

    /**
     * 通道Id
     */
    private String channelId;

    /**
     * 存储桶
     */
    private String materialBucket;

    /**
     * 文件名
     */
    private String materialName;

    /**
     * 文件oss地址
     */
    private String materialOssUrl;

    /**
     * 文件地址
     */
    private String materialUrl;

    /**
     * 文件时间 yyyy-MM-dd
     */
    private String materialDate;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 是否删除;y:是 n:否
     */
    private String isDelete;
}