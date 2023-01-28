package paddi.remoting.transport.netty.client;

import com.paddi.enums.CompressType;
import com.paddi.enums.SerializationType;
import com.paddi.factory.SingletonFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import paddi.remoting.constants.RpcConstants;
import paddi.remoting.dto.RpcMessage;
import paddi.remoting.dto.RpcResponse;

import java.net.InetSocketAddress;

/**
 * Customize the client ChannelHandler to process the data sent by the server
 *
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月26日 22:52:50
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive message: [{}]", msg);
            if(msg instanceof RpcMessage) {
                RpcMessage rpcMessage = (RpcMessage) msg;
                byte messageType = rpcMessage.getMessageType();
                if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", rpcMessage.getData());
                }else {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) rpcMessage.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            //Ensure that ByteBuf is released, otherwise there may be memory leaks
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if(state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = RpcMessage.builder()
                                             .codec(SerializationType.HESSIAN.getCode())
                                             .compress(CompressType.GZIP.getCode())
                                             .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                                             .data(RpcConstants.PING)
                                             .build();
                channel.writeAndFlush(rpcMessage);
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * Called when an exception occurs in processing a client message
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
