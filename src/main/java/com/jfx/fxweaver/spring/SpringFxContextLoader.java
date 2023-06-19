package com.jfx.fxweaver.spring;



import com.jfx.fxweaver.core.FxContextLoader;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.Supplier;

/**
 * SpringFxContextLoader.
 *
 * @author Rene Gielen
 */
public class SpringFxContextLoader extends FxContextLoader<SpringFxWeaver> {

    private final Supplier<ConfigurableApplicationContext> contextLoader;

    public SpringFxContextLoader(Supplier<ConfigurableApplicationContext> contextLoader) {
        this.contextLoader = contextLoader;
    }

    @Override
    public SpringFxWeaver start() {
        ConfigurableApplicationContext context = contextLoader.get();
        return context.getBean(SpringFxWeaver.class);
    }
}
