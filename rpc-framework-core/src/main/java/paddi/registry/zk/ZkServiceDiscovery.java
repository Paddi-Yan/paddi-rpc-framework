package paddi.registry.zk;

import com.paddi.enums.RpcErrorMessageType;
import com.paddi.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.util.CollectionUtils;
import paddi.loadbalance.LoadBalance;
import paddi.loadbalance.impl.RandomLoadBalance;
import paddi.registry.ServiceDiscovery;
import paddi.registry.zk.util.CuratorUtils;
import paddi.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 14:00:39
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;
    public ZkServiceDiscovery() {
        this.loadBalance = new RandomLoadBalance();
    }
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if(CollectionUtils.isEmpty(serviceList)) {
            throw new RpcException(RpcErrorMessageType.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        //load balancing
        String targetServiceAddress = loadBalance.selectServiceAddress(serviceList, rpcRequest);
        log.info("found the service address [{}] successfully", targetServiceAddress);
        String[] socketAddressArray = targetServiceAddress.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
