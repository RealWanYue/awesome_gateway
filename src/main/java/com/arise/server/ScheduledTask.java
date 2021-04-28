package com.arise.server;

import org.jetbrains.annotations.NotNull;

/**
 * @Author: wy
 * @Date: Created in 18:12 2021-04-26
 * @Description: 调度任务
 * @Modified: By：
 */
public class ScheduledTask implements Comparable<Object> {

    private final int fd;

    //超时(毫秒)
    private final int timeout;

    private int compare;

    public ScheduledTask(int fd, int timeout) {
        this.fd = fd;
        this.timeout = timeout;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        return 0;
    }

    public int getFd() {
        return this.fd;
    }

    public int getTimeout() {
        return this.timeout;
    }
}
