package com.kuo.easychat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.Setter;

/**
 * @author Administrator
 */
public class WebSocketServer {

    @Setter
    private String contextPath;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    public WebSocketServer(final String contextPath) {
        this.contextPath = contextPath;
    }

    public void start(final short port) {
        this.init();

        try {
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("server 启动成功。。。，端口号:"+port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.option(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 2014);

        // 初始化线程池
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup(5);

        // 初始化bootstrap配置
        serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                        pipeline.addLast(new WebSocketServerProtocolHandler("/chat"));
                        pipeline.addLast(new WebSocketHandler());
                    }
                });
    }

    private static class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            System.out.println("connected from address:" + ctx.channel().remoteAddress());
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            System.out.println("connection closed with address:" + ctx.channel().remoteAddress());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object o) {
            System.out.println("receive data:" + o + "from address:" + ctx.channel().remoteAddress());
            if (!(o instanceof TextWebSocketFrame)) {
                System.out.println("receive error type message.o:"+ o);
                return;
            }
            TextWebSocketFrame request = (TextWebSocketFrame) o;
            System.out.println("receive text:" + request.text());
            ctx.writeAndFlush(new TextWebSocketFrame("服务端返回："+request.text()));
        }
    }
}
