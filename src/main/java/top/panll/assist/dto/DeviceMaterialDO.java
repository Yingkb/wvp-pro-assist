package top.panll.assist.dto;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.tianque.light.oss.sdk.model.PutObjectResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.panll.assist.entity.DeviceMaterial;

import java.util.Date;

/**
 * @des:
 * @author: Yingkb
 * @create: 2022/03/15 13:43
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceMaterialDO {
    private PutObjectResult putObjectResult;
    private String fileName;
    private String fileUrl;
    private String deviceId;
    private String bucket;
    private String channelId;

    public DeviceMaterial buildEntity() {
        Date now = new Date();
        String materialDate = DateUtil.format(now, DatePattern.NORM_DATE_FORMATTER);
        return DeviceMaterial.builder()
                .channelId(this.channelId)
                .deviceId(this.deviceId)
                .materialBucket(this.bucket)
                .materialName(this.fileName)
                .materialOssUrl(this.putObjectResult.getOssUrl())
                .materialUrl(this.fileUrl)
                .materialDate(materialDate)
                .createdTime(now)
                .build();
    }
}
