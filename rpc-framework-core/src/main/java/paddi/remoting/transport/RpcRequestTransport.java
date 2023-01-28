package paddi.remoting.transport;

import com.paddi.extension.SPI;
import paddi.remoting.dto.RpcRequest;

/**
 * send RpcRequest
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月25日 00:42:55
 */
@SPI
public interface RpcRequestTransport {

    /**
     * send rpcRequest to server and get result
     * @param rpcRequest messageBody
     * @return data from server provider
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
