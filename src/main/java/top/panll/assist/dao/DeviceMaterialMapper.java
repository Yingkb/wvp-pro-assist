package top.panll.assist.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.panll.assist.entity.DeviceMaterial;

@Mapper
public interface DeviceMaterialMapper extends BaseMapper<DeviceMaterial> {
    int deleteByPrimaryKey(Integer id);

    int insert(DeviceMaterial record);

    int insertSelective(DeviceMaterial record);

    DeviceMaterial selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DeviceMaterial record);

    int updateByPrimaryKey(DeviceMaterial record);
}