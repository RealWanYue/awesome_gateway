package com.arise.server.route;

import com.arise.server.StandardHttpMessage;
import com.arise.server.route.pool.RemoteChannelPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.LastHttpContent;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;

/**
 * @Author: wy
 * @Date: Created in 22:47 2021-06-01
 * @Description:
 * @Modified: By：
 */
@Slf4j
public class ForwardHandler extends ChannelInboundHandlerAdapter {

    private final EpollSocketChannel forwardChannel;

    public ForwardHandler(EpollSocketChannel forwardChannel) {
        this.forwardChannel = forwardChannel;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        //处理背压
        forwardChannel.config().setAutoRead(ctx.channel().isWritable());
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        SocketChannel channel = (SocketChannel) ctx.channel();
        if (forwardChannel.isActive()) {
            forwardChannel.writeAndFlush(msg).addListener(future -> {
                if (msg instanceof LastHttpContent && future.isDone()) {
                    try {
                        RemoteChannelPool.releaseChannel(channel);
                    } catch (NoSuchElementException e) {
                        e.printStackTrace();
                        System.err.println(msg.getClass());
                        System.err.println(channel.pipeline());
                        System.err.println(channel.isActive());
                    }
                    log.debug("释放连接：{}", ctx.channel().toString());
                }
            });
        } else {
            RemoteChannelPool.releaseChannel(channel);
            log.debug("inbound 关闭 ，释放连接：{}", ctx.channel().toString());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
//        log.error("ForwardHandler:{}", "channelInactive");
        RemoteChannelPool.releaseChannel((SocketChannel) ctx.channel());
        if (forwardChannel.isActive()) {
            forwardChannel.pipeline().remove(HttpResponseEncoder.class);
            StandardHttpMessage._200.toByteBuf(ctx).forEach(e ->
                    forwardChannel.writeAndFlush(((ByteBuf) e).retainedDuplicate()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("ForwardHandler:{}", cause.getMessage());
        RemoteChannelPool.releaseChannel((SocketChannel) ctx.channel());
        ctx.close();
    }
}
