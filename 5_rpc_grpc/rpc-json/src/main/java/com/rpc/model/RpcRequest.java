package com.rpc.model;

import java.util.List;

public class RpcRequest {
  private String jsonrpc = "2.0";
  private String method;
  private List<Double> params;
  private String id;

  public RpcRequest() {}

  public RpcRequest(String method, List<Double> params, String id) {
    this.method = method;
    this.params = params;
    this.id = id;
  }

  public String getJsonrpc() {
    return jsonrpc;
  }

  public void setJsonrpc(String jsonrpc) {
    this.jsonrpc = jsonrpc;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public List<Double> getParams() {
    return params;
  }

  public void setParams(List<Double> params) {
    this.params = params;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
