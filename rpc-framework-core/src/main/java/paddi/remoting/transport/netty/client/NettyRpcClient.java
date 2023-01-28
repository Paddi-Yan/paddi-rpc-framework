package paddi.remoting.transport.netty.client;

import com.paddi.enums.CompressType;
import com.paddi.enums.SerializationType;
import com.paddi.extension.ExtensionLoader;
import com.paddi.factory.SingletonFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import paddi.registry.ServiceDiscovery;
import paddi.remoting.constants.RpcConstants;
import paddi.remoting.dto.RpcMessage;
import paddi.remoting.dto.RpcRequest;
import paddi.remoting.dto.RpcResponse;
import paddi.remoting.transport.RpcRequestTransport;
import paddi.remoting.transport.netty.codec.RpcMessageDecoder;
import paddi.remoting.transport.netty.codec.RpcMessageEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月25日 00:51:40
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {

    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        //initialize resources such as EventLoopGroup, Bootstrap
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //The timeout period of the connection
                //If this time is exceeded or the connection cannot be established, the connection fails.
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //If no data is sent to the server within 15 seconds, a heartbeat request is sent
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * connect server and get the channel, so that you can send rpc message to server
     * @param inetSocketAddress server address
     * @return channel
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            }else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //build return value
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        //get server address
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);
        if(channel.isActive()) {
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder()
                                         .data(rpcRequest)
                                         .codec(SerializationType.HESSIAN.getCode())
                                         .compress(CompressType.GZIP.getCode())
                                         .messageType(RpcConstants.REQUEST_TYPE)
                                         .build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if(future.isSuccess()) {
                    log.info("client send message successfully: [{}]", rpcMessage);
                }else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("send failed: ", future.cause());
                }
            });
        }else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if(channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }


}
