package top.panll.assist;

import com.alibaba.fastjson.JSON;
import com.tianque.light.oss.sdk.model.PutObjectResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.panll.assist.dao.DeviceChannelMapper;
import top.panll.assist.entity.DeviceChannel;
import top.panll.assist.utils.TqOSSClientUtil;

import java.io.File;

@SpringBootTest
class WvpProAssistApplicationTests {
    @Autowired
    private DeviceChannelMapper deviceChannelMapper;

    @SneakyThrows
    @Test
    public void uploadFile() {
        File file = new File("C:\\Users\\TQ-BJB0280\\Desktop\\18_16_33-18_20_31-238605.mp4");
        PutObjectResult putObjectResult = TqOSSClientUtil.putObject(file, "001.png", "06146CAF");
        System.out.println(JSON.toJSONString(putObjectResult));
        String internalEndpointUrlByUri = TqOSSClientUtil.getInternalEndpointUrlByUri(putObjectResult.getOssUri());
        System.out.println(internalEndpointUrlByUri);
    }

    @Test
    public void selectByStreamId() {
        DeviceChannel deviceChannel = deviceChannelMapper.selectByStreamId("06146CAF");
        System.out.println(JSON.toJSONString(deviceChannel));
    }

    @Test
    public void searchByEntity() {
        DeviceChannel deviceChannel = new DeviceChannel();
        deviceChannel.setChannelId("34020000001320000001");
        deviceChannel.setDeviceId("7010200492000000009");
        deviceChannel = deviceChannelMapper.searchByEntity(deviceChannel);
        System.out.println(JSON.toJSONString(deviceChannel));
    }

}
