
# Part1 Chapter1 소개

## 1.1  준비

[Downloads](https://netty.io/downloads.html)

## 1.2 개발 환경 설정

[Netty lib in mvn repository](https://mvnrepository.com/search?q=netty)


## 1.3 Discard 서버

<details>
<summary>DiscardServer</summary>

```java
package com.github.nettybook.ch1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DiscardServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new DiscardServerHandler()); // 접속된 클라이언트로부터 수신된 데이터를 처리할 핸들러 지정
                }
           });

            ChannelFuture f = b.bind(8888).sync(); // bind 메서드로 접속할 포트 지정

            f.channel().closeFuture().sync();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
```

<a href="https://github.com/krisjey/netty.book.kor/blob/master/example/src/java/com/github/nettybook/ch1/DiscardServer.java">Refer</a>

</details>

<details>
<summary>DiscardServerHandler</summary>

```java
package com.github.nettybook.ch1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a server-side channel.
 */
public class DiscardServerHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception { // 클라이언트 데이터 전송시 해당 메서드 실행
        // 아무것도 하지 않음.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // Exception 핸들링
        cause.printStackTrace();
        ctx.close();
    }
}
```

[Explain about channelRead0](https://github.com/netty/netty/wiki/New-and-noteworthy-in-5.0)

<a href="https://github.com/krisjey/netty.book.kor/blob/master/example/src/java/com/github/nettybook/ch1/DiscardServerHandler.java">Refer</a>

</details>

## 1.4 에코 서버

### 1.4.1 서버 구현

<details>
<summary>EchoServer</summary>

```java
package com.github.nettybook.ch1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EchoServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new EchoServerHandler()); // 핸들러에서 수신한 데이터 반환
                }
            });

            ChannelFuture f = b.bind(8888).sync();

            f.channel().closeFuture().sync();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
```

<a href="https://github.com/krisjey/netty.book.kor/blob/master/example/src/java/com/github/nettybook/ch1/EchoServer.java">Refer</a>

</details>

<details>
<summary>EchoServerHandler</summary>

```java
package com.github.nettybook.ch1;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handles a server-side channel.
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // 수신 이벤트 처리 메서드, 데이터 수신시 네티가 자동 호출
        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset()); // 바이트 버퍼 객체로 부터 문자열 읽음

        StringBuilder builder = new StringBuilder();
        builder.append("수신한 문자열 [");
        builder.append(readMessage);
        builder.append("]");

        System.out.println(builder.toString());

        ctx.write(msg); // ChannelHandlerContext 인터페이스, 채널 파이프라인에 대한 이벤트를 처리
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) { // channelRead 이벤트 처리 완료 후 자동으로 수행되는 이벤트 메서드
        ctx.flush(); // 채널 파이프라인에 저장된 버퍼를 전송하는 flush 메서드 호출
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
```

상속 구조  
ChannelInboundHandlerAdapter <- SimpleChannelInboundHandler

두 클래스 모두 기본 제공 클래스 수신한 데이터를 처리하는 이벤트 제공  
SimpleChannelInboundHandler 는 데이터 수신되었을때 호출되는 channelRead 이벤트에 대한 처리가 이미 구현되어 있다  

<a href="https://github.com/krisjey/netty.book.kor/blob/master/example/src/java/com/github/nettybook/ch1/EchoServerHandler.java">Refer</a>

</details>

### 1.4.2 클라이언트 구현

<details>
<summary>EchoClient</summary>

```java
package com.github.nettybook.ch1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.  Simply put, the echo client initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public final class EchoClient {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group) // 서버에 연결된 채널 하나만 존재 따라서 이벤트 루프 그룹이 하나다
             .channel(NioSocketChannel.class) // 클라이언트 어플리케이션이 생성하는 채널의 종류를 설정, 소켓 채널은 NIO로 동작
             .handler(new ChannelInitializer<SocketChannel>() { // 채널 파이프라인의 설정에 일반 소켓 채널 클래스인 SocketChannel 설정
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(new EchoClientHandler());
                 }
             });

            ChannelFuture f = b.connect("localhost", 8888).sync(); // 비동기 입출력 메서드 connect 호출
            // ChannelFuture 객체를 통해서 비동기 메서드의 처리 결과를 확인

            f.channel().closeFuture().sync(); // sync 메서드는 ChannelFuture 객체의 요청이 완료될 때까지 대기
            // 즉 connect 메서드의 처리가 완료될 때까지 대기
        }
        finally {
            group.shutdownGracefully();
        }
    }
}
```

<a href="https://github.com/krisjey/netty.book.kor/blob/master/example/src/java/com/github/nettybook/ch1/EchoClient.java">Refer</a>

</details>

<details>
<summary>EchoClientHandler</summary>

```java
package com.github.nettybook.ch1;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handler implementation for the echo client. It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class EchoClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) { // 소켓 채널이 최초 활성화되었을 때 실행
        String sendMessage = "Hello netty";

        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMessage.getBytes());

        StringBuilder builder = new StringBuilder();
        builder.append("전송한 문자열 [");
        builder.append(sendMessage);
        builder.append("]");

        System.out.println(builder.toString());
        ctx.writeAndFlush(messageBuffer); // writeAndFlush 내부적으로 데이터 기록과 전송 두가지 메서드를 호출
        // 1. 채널에 데이터를 기록 write 2. 채널에 기록된 데이터를 서버로 전송 flush
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());

        StringBuilder builder = new StringBuilder();
        builder.append("수신한 문자열 [");
        builder.append(readMessage);
        builder.append("]");

        System.out.println(builder.toString());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.close(); // 수신된 데이터를 모두 읽은 후 서버와 연결된 채널 종료
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

<a href="https://github.com/krisjey/netty.book.kor/blob/master/example/src/java/com/github/nettybook/ch1/EchoClientHandler.java">Refer</a>

</details>

### 1.4.3 데이터 이동의 방향성

Inbound <> Outbound 이벤트로 구분한 추상화 모델 제공  

- 이벤트들을 논리적으로 구분하여 고수준의 추상화 모델 제공
- 간단한 코드 작성으로 안정적인 애플리케이션을 빠르게 개발

네트워크 송수신을 추상화하기 위하여 이벤트 모델을 정의

- Inbound Event - 데이터 수신
- Outbound Event - 데이터 송신

## 1.5 마치며

성능 테스트 결과

- https://www.techempower.com - [view the results](https://www.techempower.com/benchmarks)

