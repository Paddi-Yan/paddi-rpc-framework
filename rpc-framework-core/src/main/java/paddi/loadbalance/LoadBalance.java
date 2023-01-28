package paddi.loadbalance;

import paddi.remoting.dto.RpcRequest;

import java.util.List;

/**
 * Interface to the load balancing policy
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 14:05:03
 */
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceList, RpcRequest rpcRequest);
}
