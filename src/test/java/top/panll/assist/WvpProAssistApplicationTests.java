package top.panll.assist;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.panll.assist.controller.bean.WVPResult;
import top.panll.assist.dto.MinioFileDTO;
import top.panll.assist.enums.SuccessMsgEnum;
import top.panll.assist.service.VideoFileService;
import top.panll.assist.utils.MinioFileUtil;

@SpringBootTest
class WvpProAssistApplicationTests {

    @Autowired
    private VideoFileService videoFileService;

    @Test
    void upload() {
        MinioFileDTO  minioFileDTO = MinioFileDTO.builder()
                .bucket("tq-02")
                .fileName("0203.mp4")
                .fileRootName("01/20220310")
                .filePath("C:\\Users\\TQ-BJB0280\\Desktop\\001.mp4")
                .build();
        WVPResult<String> wvpResult = MinioFileUtil.uploadFile(minioFileDTO);
        System.out.println(JSON.toJSONString(wvpResult));
    }

    @Test
    void removeBucket(){
        WVPResult<String> wvpResult = MinioFileUtil.removeBucket("test-01");
        System.out.println(JSON.toJSONString(wvpResult));
    }

    @Test
    void removeFile(){
        MinioFileDTO  minioFileDTO = MinioFileDTO.builder()
                .bucket("tq-02")
                .fileName("0203.mp4")
                .fileRootName("01/20220310")
                .build();
        WVPResult<String> wvpResult = MinioFileUtil.removeObj(minioFileDTO);
        System.out.println(JSON.toJSONString(wvpResult));
    }

    @Test
    void getUrl(){
        MinioFileDTO  minioFileDTO = MinioFileDTO.builder()
                .bucket("tq-02")
                .fileName("0203.mp4")
                .fileRootName("01/20220310")
                .build();
        WVPResult<String> wvpResult = MinioFileUtil.getObjectUrl(minioFileDTO);
        System.out.println(JSON.toJSONString(wvpResult));
    }

    @Test
    void handle(){

    }




}
