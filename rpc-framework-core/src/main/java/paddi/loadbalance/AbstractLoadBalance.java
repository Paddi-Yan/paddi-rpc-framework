package paddi.loadbalance;

import org.springframework.util.CollectionUtils;
import paddi.remoting.dto.RpcRequest;

import java.util.List;

/**
 * Abstract class for a load balancing policy
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 14:06:28
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if(CollectionUtils.isEmpty(serviceAddresses)) {
            return null;
        }
        if(serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);
}
