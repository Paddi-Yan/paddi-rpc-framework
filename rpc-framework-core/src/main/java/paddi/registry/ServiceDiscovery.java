package paddi.registry;

import paddi.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月25日 00:53:45
 */
public interface ServiceDiscovery {
    /**
     * lookup service by rpcServiceName
     * @param rpcRequest rpcRequest
     * @return service provider address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
