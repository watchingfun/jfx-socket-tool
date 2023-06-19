package com.jfx.utils;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.jfx.utils.theme.DwmAttribute;
import com.jfx.utils.theme.StageOps;
import com.sun.glass.ui.Window;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ThemeUtil {
    final static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("B", Locale.CHINA);

    private static Theme CURRENT_THEME;

    private static Theme DARK_THEME = new PrimerDark();

    private static Theme LIGHT_THEME = new PrimerLight();

    public static Theme getTheme() {
        if (List.of("凌晨", "清晨", "晚上").contains(dateTimeFormat.format(LocalDateTime.now()))) {
            return DARK_THEME;
        } else {
            return LIGHT_THEME;
        }
    }

    public static void animateThemeChange(Scene scene, Duration duration) {
        Image snapshot = scene.snapshot(null);
        Pane root = (Pane) scene.getRoot();

        ImageView imageView = new ImageView(snapshot);
        root.getChildren().add(imageView); // add snapshot on top

        var transition = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(imageView.opacityProperty(), 1, Interpolator.EASE_OUT)),
                new KeyFrame(duration, new KeyValue(imageView.opacityProperty(), 0, Interpolator.EASE_OUT))
        );
        transition.setOnFinished(e -> root.getChildren().remove(imageView));
        transition.play();
    }

    public static void initTheme() {
        CURRENT_THEME = getTheme();
        Application.setUserAgentStylesheet(CURRENT_THEME.getUserAgentStylesheet());
    }


    public static void setWindowTheme() {
        Platform.runLater(() -> {
            for (Window window : Window.getWindows()) {
                final var handle = StageOps.findWindowHandle(window);
                // Optionally enable the dark mode:
                StageOps.dwmSetBooleanValue(handle, DwmAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, CURRENT_THEME.isDarkMode());
            }
        });
    }

    public static void switchTheme(Stage stage) {
        if (CURRENT_THEME == null) {
            return;
        }
        animateThemeChange(stage.getScene(), Duration.millis(750));
        CURRENT_THEME = CURRENT_THEME.isDarkMode() ? LIGHT_THEME : DARK_THEME;
        Application.setUserAgentStylesheet(CURRENT_THEME.getUserAgentStylesheet());
        setWindowTheme();
    }
}
