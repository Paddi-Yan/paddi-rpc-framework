package paddi.remoting.dto;

import lombok.*;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月26日 23:14:03
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class RpcMessage {
    /**
     * rpc message type
     */
    private byte messageType;
    /**
     * serialization type
     */
    private byte codec;
    /**
     * compress type
     */
    private byte compress;
    /**
     * request id
     */
    private int requestId;
    /**
     * request data
     */
    private Object data;
}
