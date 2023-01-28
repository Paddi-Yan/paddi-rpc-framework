package com.paddi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月26日 23:16:31
 */
@AllArgsConstructor
@Getter
public enum SerializationType {
    KYRO((byte) 0x01, "kyro"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    HESSIAN((byte) 0x03, "hessian")
    ;
    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for(SerializationType type : SerializationType.values()) {
            if(type.getCode() == code) {
                return type.name;
            }
        }
        return null;
    }
}
