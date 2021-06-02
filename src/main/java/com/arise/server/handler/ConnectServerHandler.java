package com.arise.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;

/**
 * @Author: wy
 * @Date: Created in 22:47 2021-06-01
 * @Description:
 * @Modified: By：
 */
public final class ConnectServerHandler extends ChannelInboundHandlerAdapter {

    private final Promise<Channel> promise;

    public ConnectServerHandler(Promise<Channel> promise) {
        this.promise = promise;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
        promise.setSuccess(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        promise.setFailure(throwable);
    }
}
