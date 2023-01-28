package com.paddi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月26日 23:22:39
 */
@AllArgsConstructor
@Getter
public enum CompressType {

    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressType c : CompressType.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
