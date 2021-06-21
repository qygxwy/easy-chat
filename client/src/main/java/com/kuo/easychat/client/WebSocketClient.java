package com.kuo.easychat.client;

import com.alibaba.fastjson.JSONObject;
import com.kuo.easychat.common.action.Action;
import com.kuo.easychat.common.event.EventPool;
import com.kuo.easychat.common.event.IEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;

/**
 * Created on 2021/6/18.
 *
 * @author Fagan Wang
 */
public class WebSocketClient {
    private URI uri;

    private Bootstrap bootstrap;
    private EventLoopGroup group;

    private ChannelPromise channelPromise;

    private Channel channel;

    public WebSocketClient(final URI uri) {
        this.uri = uri;
        this.init();
    }

    public void connect() {
        try {
            channel = bootstrap.connect(uri.getHost(),uri.getPort()).sync().channel();
            channelPromise.sync();
            System.out.println("connected success! and handsshake complete!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
        bootstrap.option(ChannelOption.TCP_NODELAY,true);

        group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSctpChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(64*1024));
                        pipeline.addLast(new WebSocketHandler(getHandshaker(uri)));
                    }

                    private WebSocketClientHandshaker getHandshaker(final URI uri) {
                        return WebSocketClientHandshakerFactory.newHandshaker(uri,
                                WebSocketVersion.V13,null,false,null);
                    }
                });
    }

    private class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

        private WebSocketClientHandshaker handshaker;

        public WebSocketHandler(final WebSocketClientHandshaker handshaker) {
            this.handshaker = handshaker;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            channelPromise = ctx.newPromise();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            this.handshaker.handshake(channel);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object o) {
            System.out.println("receive data:"+o+" from address: "+ ctx.channel().remoteAddress());
            if (!handshaker.isHandshakeComplete()) {
                handshaker.finishHandshake(ctx.channel(),(FullHttpResponse) o);
                channelPromise.setSuccess();
                System.out.println("handshake success!");
                return;
            }
            if (!(o instanceof TextWebSocketFrame)) {
                System.out.println("receive no text data:" + o);
                return;
            }
            TextWebSocketFrame request = (TextWebSocketFrame) o;
            System.out.println("receive text:" + request.text());
            Action action;
            try {
                action = JSONObject.parseObject(request.text(), Action.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("transfer to object from json string failed. data: " + request.text());
                return ;
            }
            IEvent<Action, Action> event = EventPool.getInstance().find(action.getAction());
            if ( null == event ) {
                System.out.println("no event found for key: " + action.getAction());
                return ;
            }
            Action respAction = event.handle(action, ctx.channel());
            if ( null != respAction ) {
                System.out.println("resp action: " + action);
                ctx.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(respAction)));
            }
        }
    }
}
