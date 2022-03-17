package top.panll.assist.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.tianque.light.oss.sdk.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.panll.assist.controller.bean.WVPResult;
import top.panll.assist.dao.DeviceChannelMapper;
import top.panll.assist.dao.DeviceMaterialMapper;
import top.panll.assist.dto.DeviceMaterialDO;
import top.panll.assist.entity.DeviceChannel;
import top.panll.assist.enums.ErrorMsgEnum;
import top.panll.assist.utils.Constants;
import top.panll.assist.utils.TqOSSClientUtil;

import java.io.File;

/**
 * @des:
 * @author: Yingkb
 * @create: 2022/03/15 11:20
 **/
@Component
public class TqOssService {
    private final static Logger logger = LoggerFactory.getLogger(TqOssService.class);

    @Autowired
    private DeviceChannelMapper deviceChannelMapper;
    @Autowired
    private DeviceMaterialMapper deviceMaterialMapper;


    public WVPResult<String> uploadFile(String filePath, String userSettingPath) {
        String replaceStr = userSettingPath + Constants.RTP_STR;
        String newPath = filePath.replace(userSettingPath + Constants.RTP_STR, StrUtil.EMPTY);
        logger.info("replaceStr:{} newPath:{}", replaceStr, newPath);
        String[] split = newPath.split(Constants.FILE_SEPARATOR);
        String streamId = split[0];
        DeviceChannel queryDTO = new DeviceChannel();
        if (streamId.indexOf(Constants.SPLIT_BAR) > 0) {
            String[] streams = streamId.split(Constants.SPLIT_BAR);
            queryDTO.setChannelId(streams[1]);
            queryDTO.setDeviceId(streams[0]);
        } else {
            queryDTO.setDeviceId(streamId);
        }
        DeviceChannel deviceChannel = deviceChannelMapper.searchByEntity(queryDTO);
        if (null == deviceChannel) {
            return WVPResult.buildErrorResult(ErrorMsgEnum.RECORD_IS_NULL);
        }
        String deviceId = deviceChannel.getDeviceId();
        String fileName = split[2];
        File file = new File(filePath);
        PutObjectResult putObjectResult = TqOSSClientUtil.putObject(file, fileName, deviceId);
        if (null != putObjectResult) {
            String fileUrl = TqOSSClientUtil.getInternalEndpointUrlByUri(putObjectResult.getOssUri());
            DeviceMaterialDO deviceMaterialDO = DeviceMaterialDO.builder()
                    .channelId(deviceChannel.getChannelId())
                    .bucket(TqOSSClientUtil.bucket)
                    .deviceId(deviceId)
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .putObjectResult(putObjectResult)
                    .build();
            int result = deviceMaterialMapper.insertSelective(deviceMaterialDO.buildEntity());
            FileUtil.del(file);
            return result > 0 ? WVPResult.buildSuccessResult() : WVPResult.buildErrorResult(ErrorMsgEnum.FAILURE);
        }
        return WVPResult.buildErrorResult(ErrorMsgEnum.FAILURE);
    }


}
