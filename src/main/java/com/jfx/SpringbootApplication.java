package com.jfx;

import com.jfx.application.SpringbootJavaFxApplication;
import com.jfx.fxweaver.core.FxControllerAndView;
import com.jfx.fxweaver.core.FxWeaver;
import com.jfx.fxweaver.spring.InjectionPointLazyFxControllerAndViewResolver;
import javafx.scene.Node;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.Locale;
import java.util.ResourceBundle;

@SpringBootApplication
public class SpringbootApplication {

    public static void main(String[] args) {
        javafx.application.Application.launch(SpringbootJavaFxApplication.class, args);
    }


//    @Bean
//    public FxWeaver fxWeaver(ConfigurableApplicationContext applicationContext) {
//        // Would also work with javafx-weaver-core only:
//        // return new FxWeaver(applicationContext::getBean, applicationContext::close);
//        return new SpringFxWeaver(applicationContext);
//    }

    /**
     * https://github.com/rgielen/javafx-weaver
     * <p/>
     * <strong>MUST be in scope prototype!</strong>
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <C, V extends Node> FxControllerAndView<C, V> controllerAndView(FxWeaver fxWeaver,
                                                                           InjectionPoint injectionPoint) {
        return new InjectionPointLazyFxControllerAndViewResolver(fxWeaver)
                .resolve(injectionPoint);
    }

    @Bean
    public ResourceBundle resourceBundle(){
        return ResourceBundle.getBundle("lang/lang", Locale.getDefault());
    }
}
