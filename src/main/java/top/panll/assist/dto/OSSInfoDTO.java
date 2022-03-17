package top.panll.assist.dto;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @des:
 * @author: Yingkb
 * @create: 2022/03/15 14:13
 **/
@Component
@Data
public class OSSInfoDTO {
    @Value("${tqoss.ossProtocol}")
    private String ossProtocol;
    @Value("${tqoss.endpoint}")
    private String endpoint;
    @Value("${tqoss.internalEndpoint}")
    private String internalEndpoint;
    @Value("${tqoss.project}")
    private String project;
    @Value("${tqoss.bucket}")
    private String bucket;
    @Value("${tqoss.accessKey}")
    private String accessKey;
    @Value("${tqoss.accessSecret}")
    private String accessSecret;
}
