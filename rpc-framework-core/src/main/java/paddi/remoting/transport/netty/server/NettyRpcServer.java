package paddi.remoting.transport.netty.server;

import com.paddi.factory.SingletonFactory;
import com.paddi.utils.RuntimeUtil;
import com.paddi.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import paddi.config.CustomShutdownHook;
import paddi.config.RpcServiceConfig;
import paddi.provider.ServicesProvider;
import paddi.provider.impl.ZkServicesProvider;
import paddi.remoting.transport.netty.codec.RpcMessageDecoder;
import paddi.remoting.transport.netty.codec.RpcMessageEncoder;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * Receive the client message, call the corresponding method according to the client message
 * and then return the result to the client
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 16:34:21
 */
@Slf4j
@Component
public class NettyRpcServer {
    public static final int port = 9998;
    private final ServicesProvider servicesProvider = SingletonFactory.getInstance(ZkServicesProvider.class);

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        servicesProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
        );
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                     //TCP默认开启了Nagle算法 该算法是为了尽可能地发送大数据块 减少网络传输 => 关闭该选项
                    .childOption(ChannelOption.TCP_NODELAY, true)
                     //开启TCP心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                     //表示系统用于临时存放的已完成三次握手的请求队列的最大长度 如果连接建立频繁或者服务器处理创建连接较慢 可以适当调大该参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageEncoder());
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });
            //绑定端口同步等待绑定成功
            ChannelFuture future = bootstrap.bind(host, port).sync();
            //等待服务器监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }

}
