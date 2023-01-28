package paddi.registry.zk;

import org.apache.curator.framework.CuratorFramework;
import paddi.registry.ServiceRegistry;
import paddi.registry.zk.util.CuratorUtils;

import java.net.InetSocketAddress;

/**
 * 服务注册 基于Zookeeper实现
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 13:19:49
 */
public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
