package top.panll.assist.service;

import com.google.common.base.Joiner;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.panll.assist.dto.UserSettingsDTO;
import top.panll.assist.utils.RedisUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class FFmpegExecUtils implements InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(FFmpegExecUtils.class);
    @Autowired
    private UserSettingsDTO userSettings;

    @Autowired
    private RedisUtil redisUtil;

    private FFprobe ffprobe;
    private FFmpeg ffmpeg;

    public FFprobe getFfprobe() {
        return ffprobe;
    }

    public FFmpeg getFfmpeg() {
        return ffmpeg;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String ffmpegPath = userSettings.getFfmpeg();
        String ffprobePath = userSettings.getFfprobe();
        this.ffmpeg = new FFmpeg(ffmpegPath);
        this.ffprobe = new FFprobe(ffprobePath);
        logger.info("wvp-pro辅助程序启动成功。 \n{}\n{} ", this.ffmpeg.version(), this.ffprobe.version());
    }

    public interface VideoHandEndCallBack {
        void run(String status, double percentage, String result);
    }

    @Async
    public void mergeOrCutFile(List<File> fils, File dest, String destFileName, VideoHandEndCallBack callBack) {
        if (fils == null || fils.size() == 0 || ffmpeg == null || ffprobe == null || dest == null || !dest.exists()) {
            callBack.run("error", 0.0, null);
            return;
        }
        File tempFile = new File(dest.getAbsolutePath());
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        String fileListName = tempFile.getAbsolutePath() + File.separator + "fileList";
        logger.info("fileListName:{}", fileListName);
        double durationAll = 0.0;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileListName));
            for (File file : fils) {
                String[] split = file.getName().split("-");
                if (split.length != 3) continue;
                String durationStr = split[2].replace(".mp4", "");
                Double duration = Double.parseDouble(durationStr) / 1000;
                bw.write("file " + file.getAbsolutePath());
                bw.newLine();
                durationAll += duration;
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            callBack.run("error", 0.0, null);
        }
        String recordFileResultPath = dest.getAbsolutePath() + File.separator + destFileName + ".mp4";
        FFmpegBuilder builder = new FFmpegBuilder()
                .setFormat("concat")
                .overrideOutputFiles(true)
                .setInput(fileListName) // Or filename
                .addExtraArgs("-safe", "0")
                .addExtraArgs("-threads", userSettings.getThreads() + "")
                .addOutput(recordFileResultPath)
                .setVideoCodec("copy")
                .setAudioCodec("copy")
                .setFormat("mp4")
                .done();

        double finalDurationAll = durationAll;
        FFmpegJob job = executor.createJob(builder, (Progress progress) -> {
            final double duration_ns = finalDurationAll * TimeUnit.SECONDS.toNanos(1);
            double percentage = progress.out_time_ns / duration_ns;
            if (progress.status.equals(Progress.Status.END)) {
                callBack.run(progress.status.name(), percentage, recordFileResultPath);
            } else {
                callBack.run(progress.status.name(), percentage, null);
            }

        });
        job.run();
    }

    /**
     * 压缩视频
     *
     * @param convertFile 待转换的文件
     * @param targetFile  转换后的目标文件
     */
    public void toCompressFile(String convertFile, String targetFile) throws IOException {
        List<String> command = new ArrayList<>(10);
        /**将视频压缩为 每秒15帧 平均码率600k 画面的宽与高 为1280*720*/
        command.add(userSettings.getFfmpeg());
        command.add("-i");
        command.add(convertFile);
        command.add("-r");
        command.add("15");
        command.add("-b:v");
        command.add("600k");
        command.add("-s");
        command.add("1280x720");
        command.add(targetFile);
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = null;
        try {
            process = builder.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 使用这种方式会在瞬间大量消耗CPU和内存等系统资源，所以这里我们需要对流进行处理
        InputStream errorStream = process.getErrorStream();
        InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String line = "";
        while ((line = br.readLine()) != null) {
        }
        if (br != null) {
            br.close();
        }
        if (inputStreamReader != null) {
            inputStreamReader.close();
        }
        if (errorStream != null) {
            errorStream.close();
        }
        logger.info("-------------------压缩完成---转存文件--" + targetFile + "-------------");
    }


}
