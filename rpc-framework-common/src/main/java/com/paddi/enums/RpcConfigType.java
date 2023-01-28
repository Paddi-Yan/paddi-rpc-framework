package com.paddi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月26日 23:23:33
 */
@AllArgsConstructor
@Getter
public enum RpcConfigType {
    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
