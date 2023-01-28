package com.paddi.exception;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 11:12:16
 */
public class SerializeException extends RuntimeException{
    public SerializeException(String message) {
        super(message);
    }
}
