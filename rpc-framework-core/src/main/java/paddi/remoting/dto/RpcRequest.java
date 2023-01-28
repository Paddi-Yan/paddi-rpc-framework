package paddi.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月25日 00:04:29
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = -6361795692977910582L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    /**
     * 服务版本，为不兼容升级提供可能
     */
    private String version;
    /**
     * 用于处理同一个接口有多个类实现的情况
     */
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
