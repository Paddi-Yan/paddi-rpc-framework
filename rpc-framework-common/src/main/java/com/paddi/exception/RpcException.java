package com.paddi.exception;

import com.paddi.enums.RpcErrorMessageType;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 11:12:39
 */
public class RpcException extends RuntimeException{
    public RpcException(RpcErrorMessageType rpcErrorMessagetype, String detail) {
        super(rpcErrorMessagetype.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageType rpcErrorMessagetype) {
        super(rpcErrorMessagetype.getMessage());
    }
}
