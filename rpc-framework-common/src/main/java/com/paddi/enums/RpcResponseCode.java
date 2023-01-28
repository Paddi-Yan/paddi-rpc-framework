package com.paddi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月25日 00:33:17
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCode {
    SUCCESS(200, "The remote call is successful"),
    FAIL(500, "The remote call is fail");

    private final int code;

    private final String message;
}
