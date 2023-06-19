package com.jfx.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.jfx.application.SpringbootJavaFxApplication;
import com.jfx.pojo.JsonConfig;
import com.jfx.pojo.ObservableValueConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.*;

@Slf4j
public class ConfigHelper {
    private static File configFile = Paths.get("config.json").toFile();

    public static void write(JsonConfig jsonConfig) throws IOException {
        if (!configFile.exists()) {
            FileUtil.mkdir(configFile.getParentFile());
            configFile.createNewFile();
        }
        FileUtil.writeString(JSONUtil.toJsonStr(jsonConfig), configFile, StandardCharsets.UTF_8);
    }

    public static JsonConfig read() {
        if (!configFile.exists()) {
            return new JsonConfig();
        }
        String jsonStr = FileUtil.readString(configFile, StandardCharsets.UTF_8);
        if (!JSONUtil.isTypeJSON(jsonStr)) {
            throw new RuntimeException("配置文件内容非JSON格式");
        }
        return JSONUtil.readJSON(configFile, StandardCharsets.UTF_8).toBean(JsonConfig.class);
    }

    private static ScheduledExecutorService scheduledExecutorService;
    private static ScheduledFuture scheduledFuture;

    public synchronized static void startSyncToFile(ObservableValueConfig observableValueConfig) {
        if (scheduledExecutorService == null) {
            ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                    .daemon(true).namingPattern("config-syncer-%d").uncaughtExceptionHandler(SpringbootJavaFxApplication.uncaughtExceptionHandler).build();
            scheduledExecutorService = Executors.newScheduledThreadPool(1, threadFactory);
            scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
                    () -> {
                        try {
                            //long now = System.nanoTime();
                            //log.debug("同步配置到文件中...");
                            write(observableValueConfig.toJsonConfig());
                            //log.debug("同步配置到文件耗时：{}ms", TimeUnit.MILLISECONDS.convert(System.nanoTime() - now, TimeUnit.NANOSECONDS));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, 2, 2, TimeUnit.SECONDS
            );
            log.debug("启动配置同步到文件定时任务");
        }
    }

    public static void stopSync() {
        if (scheduledExecutorService != null) {
            log.debug("关闭配置同步到文件定时任务");
            scheduledFuture.cancel(true);
            scheduledExecutorService.shutdownNow();
        }
    }
}
