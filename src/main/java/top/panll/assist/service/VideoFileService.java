package top.panll.assist.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import top.panll.assist.controller.bean.WVPResult;
import top.panll.assist.dto.*;
import top.panll.assist.utils.Constants;
import top.panll.assist.utils.DateUtils;
import top.panll.assist.utils.MinioFileUtil;
import top.panll.assist.utils.RedisUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VideoFileService {

    private final static Logger logger = LoggerFactory.getLogger(VideoFileService.class);

    @Autowired
    private UserSettingsDTO userSettings;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private FFmpegExecUtils ffmpegExecUtils;


    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat simpleDateFormatForTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String keyStr = "MERGEORCUT";

    public List<File> getAppList(Boolean sort) {
        File recordFile = new File(userSettings.getRecord());
        if (recordFile.isDirectory()) {
            File[] files = recordFile.listFiles((File dir, String name) -> {
                File currentFile = new File(dir.getAbsolutePath() + File.separator + name);
                return currentFile.isDirectory();
            });
            List<File> result = Arrays.asList(files);
            if (sort != null && sort) {
                Collections.sort(result);
            }
            return result;
        } else {
            return null;
        }
    }

    public SpaceInfoDTO getSpaceInfo() {
        File recordFile = new File(userSettings.getRecord());
        SpaceInfoDTO spaceInfo = new SpaceInfoDTO();
        spaceInfo.setFree(recordFile.getFreeSpace());
        spaceInfo.setTotal(recordFile.getTotalSpace());
        return spaceInfo;
    }

    public List<File> getStreamList(String app, Boolean sort) {
        File appFile = new File(userSettings.getRecord() + File.separator + app);
        return getStreamList(appFile, sort);
    }

    public List<File> getStreamList(File appFile, Boolean sort) {
        if (appFile != null && appFile.isDirectory()) {
            File[] files = appFile.listFiles((File dir, String name) -> {
                File currentFile = new File(dir.getAbsolutePath() + File.separator + name);
                return currentFile.isDirectory();
            });
            List<File> result = Arrays.asList(files);
            if (sort != null && sort) {
                Collections.sort(result);
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * 对视频文件重命名， 00：00：00-00：00：00
     *
     * @param file
     * @throws ParseException
     */
    public void handFile(File file) {
        FFprobe ffprobe = ffmpegExecUtils.getFfprobe();
        if (file.exists() && file.isFile() && !file.getName().startsWith(".") && file.getName().endsWith(".mp4") && file.getName().indexOf(":") < 0) {
            try {
                FFmpegProbeResult in = ffprobe.probe(file.getAbsolutePath());
                double duration = in.getFormat().duration * 1000;
                logger.info("fileName:{}", file.getName());
                String endTimeStr = file.getName().replace(".mp4", "");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

                File dateFile = new File(file.getParent());
                logger.info("dateFileName:{}", dateFile.getName());
                long startTime = formatter.parse(dateFile.getName() + " " + endTimeStr).getTime();
                long durationLong = new Double(duration).longValue();
                long endTime = startTime + durationLong;
                endTime = endTime - endTime % 1000;

                String newName = file.getAbsolutePath().replace(file.getName(),
                        simpleDateFormat.format(startTime) + "-" + simpleDateFormat.format(endTime) + "-" + durationLong + ".mp4");
                boolean b = file.renameTo(new File(newName));
                if (b) {
                    logger.info("[处理文件] {}", newName);
                    WVPResult<String> wvpResult = handleFileUpload(newName, userSettings.getRecord());
                    logger.info("[处理文件上传]:{}", JSON.toJSONString(wvpResult));
                }
            } catch (IOException e) {
                logger.warn("文件可能以损坏[{}]", file.getAbsolutePath());
            } catch (ParseException e) {
                logger.error("时间格式化失败", e.getMessage());
            }
        }
    }

    public static WVPResult<String> handleFileUpload(String filePath, String userSettingPath) {
        String replaceStr = userSettingPath + Constants.RTP_STR;
        String newPath = filePath.replace(userSettingPath + Constants.RTP_STR, StrUtil.EMPTY);
        logger.info("replaceStr:{} newPath:{}", replaceStr, newPath);
        String[] split = newPath.split(MinioFileUtil.FILE_SEPARATOR);
        //TODO 需要根据streamId查deviceId
        String streamId = split[0];

        String dateStr = split[1].replaceAll("-", StrUtil.EMPTY);
        String fileName = split[2];
        String fileRootPath = streamId + MinioFileUtil.FILE_SEPARATOR + dateStr;
        MinioFileDTO minioFileDTO = MinioFileDTO.builder()
                .fileName(fileName)
                .fileRootName(fileRootPath)
                .filePath(filePath)
                .build();
        return MinioFileUtil.uploadFile(minioFileDTO);
    }

    public List<Map<String, String>> getList() {

        List<Map<String, String>> result = new ArrayList<>();

        List<File> appList = getAppList(true);
        if (appList != null && appList.size() > 0) {
            for (File appFile : appList) {
                if (appFile.isDirectory()) {
                    List<File> streamList = getStreamList(appFile.getName(), true);
                    if (streamList != null && streamList.size() > 0) {
                        for (File streamFile : streamList) {
                            Map<String, String> data = new HashMap<>();
                            data.put("app", appFile.getName());
                            data.put("stream", streamFile.getName());

                            BasicFileAttributes bAttributes = null;
                            try {
                                bAttributes = Files.readAttributes(streamFile.toPath(),
                                        BasicFileAttributes.class);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            data.put("time", simpleDateFormatForTime.format(new Date(bAttributes.lastModifiedTime().toMillis())));
                            result.add(data);
                        }
                    }
                }
            }
        }
        result.sort((Map f1, Map f2) -> {
            Date time1 = null;
            Date time2 = null;
            try {
                time1 = simpleDateFormatForTime.parse(f1.get("time").toString());
                time2 = simpleDateFormatForTime.parse(f2.get("time").toString());
            } catch (ParseException e) {
                logger.error("时间格式化失败", e.getMessage());
            }
            return time1.compareTo(time2) * -1;
        });
        return result;
    }

    /**
     * 获取制定推流的指定时间段内的推流
     *
     * @param app
     * @param stream
     * @param startTime
     * @param endTime
     * @return
     */
    public List<File> getFilesInTime(String app, String stream, Date startTime, Date endTime) {

        List<File> result = new ArrayList<>();
        if (app == null || stream == null) {
            return result;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatterForDate = new SimpleDateFormat("yyyy-MM-dd");
        String startTimeStr = null;
        String endTimeStr = null;
        if (startTime != null) {
            startTimeStr = formatter.format(startTime);
        }
        if (endTime != null) {
            endTimeStr = formatter.format(endTime);
        }

        logger.debug("获取[app: {}, stream: {}, statime: {}, endTime: {}]的视频", app, stream,
                startTimeStr, endTimeStr);

        File recordFile = new File(userSettings.getRecord());
        File streamFile = new File(recordFile.getAbsolutePath() + File.separator + app + File.separator + stream + File.separator);
        if (!streamFile.exists()) {
            logger.warn("获取[app: {}, stream: {}, statime: {}, endTime: {}]的视频时未找到目录： {}", app, stream,
                    startTimeStr, endTimeStr, stream);
            return null;
        }

        File[] dateFiles = streamFile.listFiles((File dir, String name) -> {
            Date fileDate = null;
            Date startDate = null;
            Date endDate = null;
            if (new File(dir + File.separator + name).isFile()) {
                return false;
            }
            if (startTime != null) {
                startDate = new Date(startTime.getTime() - ((startTime.getTime() + 28800000) % (86400000)));
            }
            if (endTime != null) {
                endDate = new Date(endTime.getTime() - ((endTime.getTime() + 28800000) % (86400000)));
            }
            try {
                fileDate = formatterForDate.parse(name);
            } catch (ParseException e) {
                logger.error("过滤日期文件时异常： {}-{}", name, e.getMessage());
                return false;
            }
            boolean filterResult = true;

            if (startDate != null) {
                filterResult = filterResult && DateUtils.getStartOfDay(startDate).compareTo(fileDate) <= 0;
            }

            if (endDate != null) {
                filterResult = filterResult && DateUtils.getEndOfDay(endDate).compareTo(fileDate) >= 0;
            }

            return filterResult;
        });

        if (dateFiles != null && dateFiles.length > 0) {
            for (File dateFile : dateFiles) {
                // TODO 按时间获取文件
                File[] files = dateFile.listFiles((File dir, String name) -> {
                    boolean filterResult = true;
                    File currentFile = new File(dir + File.separator + name);
                    if (currentFile.isFile() && name.contains(":") && name.endsWith(".mp4") && !name.startsWith(".") && currentFile.length() > 0) {
                        String[] timeArray = name.split("-");
                        if (timeArray.length == 3) {
                            String fileStartTimeStr = dateFile.getName() + " " + timeArray[0];
                            String fileEndTimeStr = dateFile.getName() + " " + timeArray[1];
                            try {
                                if (startTime != null) {
                                    filterResult = filterResult && (formatter.parse(fileStartTimeStr).after(startTime) || (formatter.parse(fileStartTimeStr).before(startTime) && formatter.parse(fileEndTimeStr).after(startTime)));
                                }
                                if (endTime != null) {
                                    filterResult = filterResult && (formatter.parse(fileEndTimeStr).before(endTime) || (formatter.parse(fileEndTimeStr).after(endTime) && formatter.parse(fileStartTimeStr).before(endTime)));
                                }
                            } catch (ParseException e) {
                                logger.error("过滤视频文件时异常： {}-{}", name, e.getMessage());
                                return false;
                            }
                        }
                    } else {
                        filterResult = false;
                    }
                    return filterResult;
                });

                List<File> fileList = Arrays.asList(files);
                result.addAll(fileList);
            }
        }
        if (result.size() > 0) {
            result.sort((File f1, File f2) -> {
                int sortResult = 0;
                String[] timeArray1 = f1.getName().split("-");
                String[] timeArray2 = f2.getName().split("-");
                if (timeArray1.length == 3 && timeArray2.length == 3) {
                    File dateFile1 = f1.getParentFile();
                    File dateFile2 = f2.getParentFile();
                    String fileStartTimeStr1 = dateFile1.getName() + " " + timeArray1[0];
                    String fileStartTimeStr2 = dateFile2.getName() + " " + timeArray2[0];
                    try {
                        sortResult = formatter.parse(fileStartTimeStr1).compareTo(formatter.parse(fileStartTimeStr2));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return sortResult;
            });
        }
        return result;
    }


    public String mergeOrCut(String app, String stream, Date startTime, Date endTime, String remoteHost) {
        List<File> filesInTime = this.getFilesInTime(app, stream, startTime, endTime);
        if (filesInTime == null || filesInTime.size() == 0) {
            logger.info("此时间段未未找到视频文件");
            return null;
        }
        String taskId = DigestUtils.md5DigestAsHex(String.valueOf(System.currentTimeMillis()).getBytes());
        logger.info("[录像合并] 开始合并，APP:{}, STREAM: {}, 任务ID：{}", app, stream, taskId);
        String destDir = "recordTemp" + File.separator + taskId + File.separator + app;
        File recordFile = new File(new File(userSettings.getRecord()).getParentFile().getAbsolutePath() + File.separator + destDir);
        if (!recordFile.exists()) recordFile.mkdirs();
        MergeOrCutTaskInfoDTO mergeOrCutTaskInfo = new MergeOrCutTaskInfoDTO();
        mergeOrCutTaskInfo.setId(taskId);
        mergeOrCutTaskInfo.setApp(app);
        mergeOrCutTaskInfo.setStream(stream);
        mergeOrCutTaskInfo.setCreateTime(simpleDateFormatForTime.format(System.currentTimeMillis()));
        if (startTime != null) {
            mergeOrCutTaskInfo.setStartTime(simpleDateFormatForTime.format(startTime));
        } else {
            String startTimeInFile = filesInTime.get(0).getParentFile().getName() + " "
                    + filesInTime.get(0).getName().split("-")[0];
            mergeOrCutTaskInfo.setStartTime(startTimeInFile);
        }
        if (endTime != null) {
            mergeOrCutTaskInfo.setEndTime(simpleDateFormatForTime.format(endTime));
        } else {
            String endTimeInFile = filesInTime.get(filesInTime.size() - 1).getParentFile().getName() + " "
                    + filesInTime.get(filesInTime.size() - 1).getName().split("-")[1];
            mergeOrCutTaskInfo.setEndTime(endTimeInFile);
        }
        if (filesInTime.size() == 1) {

            // 文件只有一个则不合并，直接复制过去
            mergeOrCutTaskInfo.setPercentage("1");
            // 处理文件路径
            String recordFileResultPath = recordFile.getAbsolutePath() + File.separator + stream + ".mp4";
            Path relativize = Paths.get(userSettings.getRecord()).getParent().relativize(Paths.get(recordFileResultPath));
            try {
                Files.copy(filesInTime.get(0).toPath(), Paths.get(recordFileResultPath));
            } catch (IOException e) {
                e.printStackTrace();
                logger.info("[录像合并] 失败，APP:{}, STREAM: {}, 任务ID：{}", app, stream, taskId);
                return taskId;
            }
            mergeOrCutTaskInfo.setRecordFile(relativize.toString());
            if (remoteHost != null) {
                mergeOrCutTaskInfo.setDownloadFile(remoteHost + "/download.html?url=" + relativize);
                mergeOrCutTaskInfo.setPlayFile(remoteHost + "/" + relativize);
            }
            String key = String.format("%S_%S_%S_%S", keyStr, mergeOrCutTaskInfo.getApp(), mergeOrCutTaskInfo.getStream(), mergeOrCutTaskInfo.getId());
            redisUtil.set(key, mergeOrCutTaskInfo);
            redisUtil.convertAndSend("topic_mergeorcut_end", mergeOrCutTaskInfo);
            logger.info("[录像合并] 合并完成，APP:{}, STREAM: {}, 任务ID：{}", app, stream, taskId);
        } else {
            ffmpegExecUtils.mergeOrCutFile(filesInTime, recordFile, stream, (status, percentage, result) -> {
                // 发出redis通知
                if (status.equals(Progress.Status.END.name())) {
                    mergeOrCutTaskInfo.setPercentage("1");

                    // 处理文件路径
                    Path relativize = Paths.get(userSettings.getRecord()).getParent().relativize(Paths.get(result));
                    mergeOrCutTaskInfo.setRecordFile(relativize.toString());
                    if (remoteHost != null) {
                        mergeOrCutTaskInfo.setDownloadFile(remoteHost + "/download.html?url=" + relativize);
                        mergeOrCutTaskInfo.setPlayFile(remoteHost + "/" + relativize);
                    }
                    redisUtil.convertAndSend("topic_mergeorcut_end", mergeOrCutTaskInfo);
                    logger.info("[录像合并] 合并完成，APP:{}, STREAM: {}, 任务ID：{}", app, stream, taskId);
                } else {
                    mergeOrCutTaskInfo.setPercentage(percentage + "");
                    redisUtil.convertAndSend("topic_mergeorcut_continue", mergeOrCutTaskInfo);
                }
                String key = String.format("%S_%S_%S_%S", keyStr, mergeOrCutTaskInfo.getApp(), mergeOrCutTaskInfo.getStream(), mergeOrCutTaskInfo.getId());
                redisUtil.set(key, mergeOrCutTaskInfo);
            });
        }

        return taskId;
    }

    /**
     * 获取指定时间的日期文件夹
     *
     * @param app
     * @param stream
     * @param year
     * @param month
     * @return
     */
    public List<File> getDateList(String app, String stream, Integer year, Integer month, Boolean sort) {
        File recordFile = new File(userSettings.getRecord());
        File streamFile = new File(recordFile.getAbsolutePath() + File.separator + app + File.separator + stream);
        return getDateList(streamFile, year, month, sort);
    }

    public List<File> getDateList(File streamFile, Integer year, Integer month, Boolean sort) {
        if (!streamFile.exists() && streamFile.isDirectory()) {
            logger.warn("获取[]的视频时未找到目录： {}", streamFile.getName());
            return null;
        }
        File[] dateFiles = streamFile.listFiles((File dir, String name) -> {
            File currentFile = new File(dir.getAbsolutePath() + File.separator + name);
            if (!currentFile.isDirectory()) {
                return false;
            }
            Date date = null;
            try {
                date = simpleDateFormat.parse(name);
            } catch (ParseException e) {
                logger.error("格式化时间{}错误", name);
                return false;
            }
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH) + 1;
            if (year != null) {
                if (month != null) {
                    return y == year && m == month;
                } else {
                    return y == year;
                }
            } else {
                return true;
            }

        });
        List<File> dateFileList = Arrays.asList(dateFiles);
        if (sort != null && sort) {
            dateFileList.sort((File f1, File f2) -> {
                int sortResult = 0;

                try {
                    sortResult = simpleDateFormat.parse(f1.getName()).compareTo(simpleDateFormat.parse(f2.getName()));
                } catch (ParseException e) {
                    logger.error("格式化时间{}/{}错误", f1.getName(), f2.getName());
                }
                return sortResult;
            });
        }

        return dateFileList;
    }

    public List<MergeOrCutTaskInfoDTO> getTaskListForDownload(Boolean idEnd, String app, String stream, String taskId) {
        ArrayList<MergeOrCutTaskInfoDTO> result = new ArrayList<>();
        if (app == null) app = "*";
        if (stream == null) stream = "*";
        if (taskId == null) taskId = "*";
        List<Object> taskCatch = redisUtil.scan(String.format("%S_%S_%S_%S", keyStr, app, stream, taskId));
        for (int i = 0; i < taskCatch.size(); i++) {
            String keyItem = taskCatch.get(i).toString();
            MergeOrCutTaskInfoDTO mergeOrCutTaskInfo = (MergeOrCutTaskInfoDTO) redisUtil.get(keyItem);
            if (mergeOrCutTaskInfo != null && mergeOrCutTaskInfo.getPercentage() != null) {
                if (idEnd != null) {
                    if (idEnd) {
                        if (Double.parseDouble(mergeOrCutTaskInfo.getPercentage()) == 1) {
                            result.add(mergeOrCutTaskInfo);
                        }
                    } else {
                        if (Double.parseDouble(mergeOrCutTaskInfo.getPercentage()) < 1) {
                            result.add((MergeOrCutTaskInfoDTO) redisUtil.get(keyItem));
                        }
                    }
                } else {
                    result.add((MergeOrCutTaskInfoDTO) redisUtil.get(keyItem));
                }

            }
        }
        result.sort((MergeOrCutTaskInfoDTO m1, MergeOrCutTaskInfoDTO m2) -> {
            int sortResult = 0;
            try {
                sortResult = simpleDateFormatForTime.parse(m1.getCreateTime()).compareTo(simpleDateFormatForTime.parse(m2.getCreateTime()));
                if (sortResult == 0) {
                    sortResult = simpleDateFormatForTime.parse(m1.getStartTime()).compareTo(simpleDateFormatForTime.parse(m2.getStartTime()));
                }
                if (sortResult == 0) {
                    sortResult = simpleDateFormatForTime.parse(m1.getEndTime()).compareTo(simpleDateFormatForTime.parse(m2.getEndTime()));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return sortResult * -1;
        });

        return result;
    }

    public boolean stopTask(String taskId) {
//        Runnable task = taskList.get(taskId);
//        boolean result = false;
//        if (task != null) {
//            processThreadPool.remove(task);
//            taskList.remove(taskId);
//            List<Object> taskCatch = redisUtil.scan(String.format("%S_*_*_%S", keyStr, taskId));
//            if (taskCatch.size() == 1) {
//                redisUtil.del((String) taskCatch.get(0));
//                result = true;
//            }
//        }
        return false;
    }

    public boolean collection(String app, String stream, String type) {
        File streamFile = new File(userSettings.getRecord() + File.separator + app + File.separator + stream);
        boolean result = false;
        if (streamFile.exists() && streamFile.isDirectory() && streamFile.canWrite()) {
            File signFile = new File(streamFile.getAbsolutePath() + File.separator + type + ".sign");
            try {
                result = signFile.createNewFile();
            } catch (IOException e) {
                logger.error("[收藏文件]失败，{}/{}", app, stream);
            }
        }
        return result;
    }

    public boolean removeCollection(String app, String stream, String type) {
        File signFile = new File(userSettings.getRecord() + File.separator + app + File.separator + stream + File.separator + type + ".sign");
        boolean result = false;
        if (signFile.exists() && signFile.isFile()) {
            result = signFile.delete();
        }
        return result;
    }

    public List<SignInfoDTO> getCollectionList(String app, String stream, String type) {
        List<File> appList = this.getAppList(true);
        List<SignInfoDTO> result = new ArrayList<>();
        if (appList.size() > 0) {
            for (File appFile : appList) {
                if (app != null) {
                    if (!app.equals(appFile.getName())) {
                        continue;
                    }
                }
                List<File> streamList = getStreamList(appFile, true);
                if (streamList.size() > 0) {
                    for (File streamFile : streamList) {
                        if (stream != null) {
                            if (!stream.equals(streamFile.getName())) {
                                continue;
                            }
                        }

                        if (type != null) {
                            File signFile = new File(streamFile.getAbsolutePath() + File.separator + type + ".sign");
                            if (signFile.exists()) {
                                SignInfoDTO signInfo = new SignInfoDTO();
                                signInfo.setApp(appFile.getName());
                                signInfo.setStream(streamFile.getName());
                                signInfo.setType(type);
                                result.add(signInfo);
                            }
                        } else {
                            streamFile.listFiles((File dir, String name) -> {
                                File currentFile = new File(dir.getAbsolutePath() + File.separator + name);
                                if (currentFile.isFile() && name.endsWith(".sign")) {
                                    String currentType = name.substring(0, name.length() - ".sign".length());
                                    SignInfoDTO signInfo = new SignInfoDTO();
                                    signInfo.setApp(appFile.getName());
                                    signInfo.setStream(streamFile.getName());
                                    signInfo.setType(currentType);
                                    result.add(signInfo);
                                }
                                return false;
                            });
                        }
                    }
                }
            }
        }
        return result;
    }
}
