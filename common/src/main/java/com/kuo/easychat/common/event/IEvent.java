package com.kuo.easychat.common.event;

import io.netty.channel.Channel;

/**
 * Created on 2021/6/21.
 *
 * @author Fagan Wang
 */
public interface IEvent<T, R> {
    /**
     * 处理事件业务
     *
     * @param request 请求
     * @param channel 通道
     * @return 结果
     */
    R handle(final T request, final Channel channel);
}
