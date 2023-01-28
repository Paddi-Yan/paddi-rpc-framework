package paddi.proxy;

import com.paddi.enums.RpcErrorMessageType;
import com.paddi.enums.RpcResponseCode;
import com.paddi.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import paddi.config.RpcServiceConfig;
import paddi.remoting.dto.RpcRequest;
import paddi.remoting.dto.RpcResponse;
import paddi.remoting.transport.RpcRequestTransport;
import paddi.remoting.transport.netty.client.NettyRpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Dynamic proxy class.
 * When a dynamic proxy object calls a method, it actually calls the following invoke method.
 * It is precisely because of the dynamic proxy that the remote method called by the client is like calling the local method (the intermediate process is shielded)
 *
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 14:13:45
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private static final String INTERFACE_NAME = "interfaceName";
    /**
     * send request to the server
     */
    private final RpcRequestTransport rpcRequestTransport;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    /**
     * get the proxy object
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * This method is actually called when you use a proxy object to call a method.
     * The proxy object is the object you get through the getProxy method.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoke method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                                     .parameters(args)
                                     .interfaceName(method.getDeclaringClass().getName())
                                     .paramTypes(method.getParameterTypes())
                                     .requestId(UUID.randomUUID().toString())
                                     .group(rpcServiceConfig.getGroup())
                                     .version(rpcServiceConfig.getVersion())
                                     .build();
        RpcResponse<Object> rpcResponse = null;
        if(rpcRequestTransport instanceof NettyRpcClient) {
            CompletableFuture<RpcResponse<Object>> invokeFuture = (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = invokeFuture.get();
        }
        this.check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if(rpcResponse == null) {
            throw new RpcException(RpcErrorMessageType.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        if(!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageType.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        if(rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageType.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
