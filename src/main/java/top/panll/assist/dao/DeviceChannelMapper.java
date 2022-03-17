package top.panll.assist.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.panll.assist.entity.DeviceChannel;

/**
 * @des:
 * @author: Yingkb
 * @create: 2022/03/15 10:19
 **/
@Mapper
public interface DeviceChannelMapper extends BaseMapper<DeviceChannel> {

    /**
     * 根据流Id查询deviceId
     *
     * @param streamId
     * @return
     */
    DeviceChannel selectByStreamId(String streamId);

    /**
     * 根据实体类查询
     * @param deviceChannel
     * @return
     */
    DeviceChannel searchByEntity(DeviceChannel deviceChannel);
}
