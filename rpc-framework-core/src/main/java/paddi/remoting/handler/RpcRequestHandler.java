package paddi.remoting.handler;

import com.paddi.exception.RpcException;
import com.paddi.factory.SingletonFactory;
import lombok.extern.slf4j.Slf4j;
import paddi.provider.ServicesProvider;
import paddi.provider.impl.ZkServicesProvider;
import paddi.remoting.dto.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RpcRequest Processor
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 17:18:37
 */
@Slf4j
public class RpcRequestHandler {
    private final ServicesProvider servicesProvider;

    public RpcRequestHandler() {
        servicesProvider = SingletonFactory.getInstance(ZkServicesProvider.class);
    }

    /**
     * Processing rpcRequest: call the corresponding method and then return the method
     * @return invoke result
     */
    public Object handle(RpcRequest rpcRequest) {
        Object service = servicesProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * get method execution results
     * @param rpcRequest client request
     * @param service service
     * @return the result of the target method execution
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service [{}] invoke method [{}] successfully", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
