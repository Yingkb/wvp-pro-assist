package top.panll.assist.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.panll.assist.dto.MinioConfigDTO;
import top.panll.assist.utils.MinioFileUtil;

/**
 * @des:
 * @author: Yingkb
 * @create: 2022/03/10 09:29
 **/
@Component
@Order(value = 2)
public class MinioInit implements CommandLineRunner {
    @Autowired
    private MinioConfigDTO minioConfig;

    @Override
    public void run(String... args) throws Exception {
        MinioFileUtil.initDefault(minioConfig);
    }
}
