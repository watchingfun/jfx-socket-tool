package com.jfx.application;

import com.jfx.ProgressBeanPostProcessor;
import com.jfx.SpringbootApplication;
import com.jfx.TaskBasedSplash;
import com.jfx.config.ConfigHelper;
import com.jfx.controller.MainController;
import com.jfx.utils.StageManageUtil;
import com.jfx.utils.ThemeUtil;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.io.PrintWriter;
import java.io.StringWriter;


@Slf4j
public class SpringbootJavaFxApplication extends javafx.application.Application {

    private ConfigurableApplicationContext context;
    Task<Void> task;

    @Override
    public void init() {
        task = new Task<>() {
            @Override
            protected Void call() {
//                LocalDate localDate = LocalDate.of(2023, 6, 18);
//                if (LocalDate.now().isAfter(localDate)) {
//                    throw new RuntimeException("程序试用期已过");
//                }
                ApplicationContextInitializer<GenericApplicationContext> initializer =
                        context -> {
                            context.registerBean(javafx.application.Application.class, () -> SpringbootJavaFxApplication.this);
                            context.registerBean(Parameters.class, () -> getParameters()); // for demonstration, not really needed
                            context.registerBean(ProgressBeanPostProcessor.class, () -> new ProgressBeanPostProcessor(this::updateMessage));
                        };
                context = new SpringApplicationBuilder()
                        .sources(SpringbootApplication.class)
                        .initializers(initializer).headless(false)
                        .run(getParameters().getRaw().toArray(new String[0]));
                return null;
            }
        };
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ThemeUtil.initTheme();
        Window.getWindows().addListener((ListChangeListener) c -> {
            ThemeUtil.setWindowTheme();
        });
        TaskBasedSplash taskBasedSplash = new TaskBasedSplash(() -> {
//            Stage stage = primaryStage;
//            Scene scene = new Scene(context.getBean(FxWeaver.class).loadView(MainController.class), 400, 300);
//            stage.setScene(scene);
//            stage.show();
            Stage stage = StageManageUtil.getStage(MainController.class);
            stage.show();
        }, task);
        taskBasedSplash.init();
        new Thread(task).start();

        task.setOnFailed(event -> {
            Throwable throwable = event.getSource().getException();
            throwable.printStackTrace();
            Alert alert = createErrorDialog(throwable);
            taskBasedSplash.getInitStage().close();
            alert.showAndWait();
            try {
                this.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                log.info("应用启动成功");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        if (this.context != null) {
            this.context.close();
        }
        Platform.exit();
        ConfigHelper.stopSync();
    }

    static Alert createErrorDialog(Throwable ex) {
        //获取最底层异常
        while (ex != null) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                break;
            }
            ex = cause;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("An exception occurred");
        alert.setContentText(ex.getLocalizedMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();
        Label label = new Label("The exception stacktrace was:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        return alert;
    }

    static void showErrorDialog(Throwable ex) {
        Alert alert = createErrorDialog(ex);
        Platform.runLater(alert::showAndWait);
    }

    public static Thread.UncaughtExceptionHandler uncaughtExceptionHandler =
            (t, e) -> {
                log.error("捕捉到未处理的异常：{}", e.getMessage());
                // 抛出栈信息
                e.printStackTrace();
                showErrorDialog(e);
            };

}
