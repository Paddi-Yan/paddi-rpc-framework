package paddi.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月25日 00:53:56
 */
public interface ServiceRegistry {
    /**
     * 注册服务到服务中心
     * @param rpcServiceName 完整的服务名称 (classname + group + version)
     * @param inetSocketAddress 远程服务地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
