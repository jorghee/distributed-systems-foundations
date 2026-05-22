package com.rpc.model;

public class RpcError {
  private int code;
  private String message;

  public RpcError(int code, String message) {
    this.code = code;
    this.message = message;
  }

  // JSON-RPC Estandar
  public static final RpcError METHOD_NOT_FOUND = new RpcError(-32601, "Method not found");
  public static final RpcError INVALID_PARAMS = new RpcError(-32602, "Invalid params");
  public static final RpcError INTERNAL_ERROR = new RpcError(-32603, "Internal error");

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
