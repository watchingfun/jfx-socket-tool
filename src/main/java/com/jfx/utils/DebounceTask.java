package com.jfx.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class DebounceTask<T> {
    private Timer timer;
    private final Long delay;
    private final Consumer<T> consumer;

    public DebounceTask(Consumer<T> consumer, Long delay) {
        this.consumer = consumer;
        this.delay = delay;
    }

    public static <T> DebounceTask<T> build(Consumer<T> consumer, Long delay) {
        return new DebounceTask<T>(consumer, delay);
    }

    public void run(T t) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer(true);//让主线程退出时也timer也退出
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer = null;
                consumer.accept(t);
            }
        }, delay);
    }
}
