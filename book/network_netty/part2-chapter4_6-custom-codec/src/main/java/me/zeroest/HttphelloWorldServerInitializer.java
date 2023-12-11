package me.zeroest;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class HttphelloWorldServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public HttphelloWorldServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        // 채널 파이프라인에 등록된 이벤트 핸들러의 순서 중요
        p.addLast(new HttpServerCodec()); // 네티 제공 HTTP 서버 코덱
        p.addLast(new HttpHelloWorldServerHandler());
    }

}
