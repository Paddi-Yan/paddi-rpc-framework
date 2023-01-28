package paddi.provider.impl;

import com.paddi.enums.RpcErrorMessageType;
import com.paddi.exception.RpcException;
import com.paddi.extension.ExtensionLoader;
import lombok.extern.slf4j.Slf4j;
import paddi.config.RpcServiceConfig;
import paddi.provider.ServicesProvider;
import paddi.registry.ServiceRegistry;
import paddi.remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 16:26:07
 */
@Slf4j
public class ZkServicesProvider implements ServicesProvider {

    /**
     * key: rpc service name(interfaceName + version + group)
     * value service
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServicesProvider() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("add service: {} and interfaces: {}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if(service == null) {
            throw new RpcException(RpcErrorMessageType.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.port));
        } catch(UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
