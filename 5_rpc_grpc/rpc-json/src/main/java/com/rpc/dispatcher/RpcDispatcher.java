package com.rpc.dispatcher;

import com.rpc.model.RpcError;
import com.rpc.model.RpcRequest;
import com.rpc.model.RpcResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class RpcDispatcher {
  private final Object targetService;

  public RpcDispatcher(Object targetService) {
    this.targetService = targetService;
  }

  public RpcResponse dispatch(RpcRequest request) {
    String methodName = request.getMethod();
    List<Double> params = request.getParams();

    try {
      Method method = findMethod(methodName, params.size());

      if (method == null) {
        return RpcResponse.error(RpcError.METHOD_NOT_FOUND, request.getId());
      }

      Object[] args = params.toArray();

      // Invocación Dinámica Local
      Object result = method.invoke(targetService, args);

      return RpcResponse.success(result, request.getId());

    } catch (InvocationTargetException e) {
      // El método de negocio lanzó una excepción
      RpcError customError = new RpcError(-32000, e.getTargetException().getMessage());
      return RpcResponse.error(customError, request.getId());
    } catch (Exception e) {
      return RpcResponse.error(RpcError.INTERNAL_ERROR, request.getId());
    }
  }

  private Method findMethod(String name, int paramCount) {
    for (Method method : targetService.getClass().getMethods()) {
      if (method.getName().equals(name) && method.getParameterCount() == paramCount) {
        return method;
      }
    }
    return null;
  }
}
