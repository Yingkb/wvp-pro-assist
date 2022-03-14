package top.panll.assist.dto;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @des: 文件上传
 * @author: Yingkb
 * @create: 2022/03/10 09:40
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MinioFileDTO {
    /**
     * 桶
     */
    private String bucket;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件根目录
     * 比如你想分层  设备号-时间-文件
     * fileRootNmae : 10001/20220310
     * 最后形成:10001/20220310/xxx.pdf\mp4\png
     */
    private String fileRootName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 过期时间(天）
     */
    private Integer expiryDays;


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }


}
