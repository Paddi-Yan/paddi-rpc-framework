package paddi.remoting.dto;

import com.paddi.enums.RpcResponseCode;
import lombok.*;

import java.io.Serializable;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月25日 00:27:55
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = -7731522801005706689L;
    private String requestId;
    /**
     * response code
     */
    private Integer code;
    /**
     * response message
     */
    private String message;
    /**
     * response body
     */
    private T data;
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCode.SUCCESS.getCode());
        response.setMessage(RpcResponseCode.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if(null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCode rpcResponseCode) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCode.getCode());
        response.setMessage(response.getMessage());
        return response;
    }
}
