package com.rpc.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // No serializa nulos según spec JSON-RPC
public class RpcResponse {
  private String jsonrpc = "2.0";
  private Object result;
  private RpcError error;
  private String id;

  public RpcResponse() {}

  public static RpcResponse success(Object result, String id) {
    RpcResponse response = new RpcResponse();
    response.result = result;
    response.id = id;
    return response;
  }

  public static RpcResponse error(RpcError error, String id) {
    RpcResponse response = new RpcResponse();
    response.error = error;
    response.id = id;
    return response;
  }

  public String getJsonrpc() {
    return jsonrpc;
  }

  public void setJsonrpc(String jsonrpc) {
    this.jsonrpc = jsonrpc;
  }

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public RpcError getError() {
    return error;
  }

  public void setError(RpcError error) {
    this.error = error;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
