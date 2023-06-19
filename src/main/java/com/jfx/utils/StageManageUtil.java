package com.jfx.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.jfx.fxweaver.core.FxWeaver;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Optional;

public class StageManageUtil {
    private static final HashMap<Class<?>, Stage> STAGE = new HashMap<>();
    private static final HashMap<Class<?>, Object> CONTROL = new HashMap<>();

    //在Controller里的initialize方法调用注册
    public static <V extends Node, C> Stage register(C controller, V view, ConfigFunction function) {
        Stage stage = new Stage();
        Scene scene = new Scene((Parent) view);
        stage.setScene(scene);
        function.config(stage, scene);
        STAGE.put(controller.getClass(), stage);
        CONTROL.put(controller.getClass(), controller);
        return stage;
    }

    public static <V extends Node, C> Stage register(C controller, Stage stage) {
        STAGE.put(controller.getClass(), stage);
        CONTROL.put(controller.getClass(), controller);
        return stage;
    }

    public static <V extends Node, C> Stage register(C controller, V view) {
        return register(controller, view, (stage, scene) -> {
        });
    }

    private static Optional<Stage> getOptionalStage(Class<?> clz) {
        return Optional.ofNullable(STAGE.get(clz)).or(() -> {
            tryTriggerRegister(clz); //让fxml load加载触发在Controller里的initialize方法
            return Optional.of(STAGE.get(clz));
        });
    }

    private static <T> Optional<T> getOptionalController(Class<T> clz) {
        return Optional.ofNullable(clz.cast(CONTROL.get(clz))).or(() -> {
            tryTriggerRegister(clz); //让fxml load加载触发在Controller里的initialize方法
            return Optional.of(clz.cast(CONTROL.get(clz)));
        });
    }


    private static void tryTriggerRegister(Class<?> clz) {
        SpringUtil.getBean(FxWeaver.class).load(clz);
    }

    public static Stage getStage(Class<?> clz) {
        return getOptionalStage(clz).orElseThrow(NotRegisterException::new);
    }

    public static <T> T getController(Class<T> clz) {
        return getOptionalController(clz).orElseThrow(NotRegisterException::new);
    }

    public interface ConfigFunction {
        void config(Stage stage, Scene scene);
    }

    public static class NotRegisterException extends RuntimeException {

    }
}
