package paddi.config;

import lombok.*;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 15:18:54
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * service version
     */
    private String version = "";

    /**
     * when the interface has multiple implementation classes, distinguish by group
     */
    private String group = "";

    /**
     * target service
     */
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
