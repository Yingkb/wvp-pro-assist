package top.panll.assist.utils;

import cn.hutool.core.bean.BeanUtil;
import com.tianque.light.oss.sdk.ClientConfiguration;
import com.tianque.light.oss.sdk.TqOssClient;
import com.tianque.light.oss.sdk.exception.ClientException;
import com.tianque.light.oss.sdk.exception.TqOssServerException;
import com.tianque.light.oss.sdk.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.panll.assist.dto.OSSInfoDTO;

import java.io.File;

/**
 * @des:
 * @author: Yingkb
 * @create: 2022/03/15 14:11
 **/
public class TqOSSClientUtil {
    private final static Logger logger = LoggerFactory.getLogger(TqOSSClientUtil.class);


    private static TqOssClient tqOssClient;
    private static ClientConfiguration clientConfiguration;
    public static String bucket;

    public static void initTqOssClient(OSSInfoDTO ossInfoDTO) {
        bucket = ossInfoDTO.getBucket();
        clientConfiguration = new ClientConfiguration();
        BeanUtil.copyProperties(ossInfoDTO, clientConfiguration);
        tqOssClient = new TqOssClient(clientConfiguration);
        logger.info("初始化OSSClient完毕......");
    }

    public static PutObjectResult putObject(File file, String reallyFileName, String filePathPrefix) {
        try {
            return tqOssClient.putObject(file, reallyFileName, filePathPrefix);
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (TqOssServerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getInternalEndpointUrlByUri(String uri) {
        return clientConfiguration.getInternalEndpointUrlByUri(uri, false);
    }


}
