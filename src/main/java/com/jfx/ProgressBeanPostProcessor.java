package com.jfx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ProgressBeanPostProcessor implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final Consumer<String> consumer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String s = String.format("springboot容器刷新完成, 总计 %d 个 beans 完成初始化", counter.get());
        log.debug(s);
        consumer.accept(s);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        counter.incrementAndGet();
        String s = String.format("bean %s 初始化完成 ", beanName);
        log.debug(s);
        consumer.accept(s);
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}
