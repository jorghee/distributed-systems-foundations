package com.rpc.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpc.dispatcher.RpcDispatcher;
import com.rpc.model.RpcRequest;
import com.rpc.model.RpcResponse;
import com.rpc.service.CalculatorService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcServer {
  private static final int PORT = 8080;
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final ExecutorService threadPool =
      Executors.newFixedThreadPool(10); // Multithreading
  private static final RpcDispatcher dispatcher = new RpcDispatcher(new CalculatorService());

  public static void main(String[] args) {
    System.out.println("Comenzando el servidor RPC en el puerto " + PORT + "...");

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

        // Despacha el cliente a un hilo para manejar conexiones concurrentes
        threadPool.submit(() -> handleClient(clientSocket));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void handleClient(Socket socket) {
    try (socket;
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
      String rawJson;
      // Lee línea por línea (cada línea es un Request completo)
      while ((rawJson = in.readLine()) != null) {
        System.out.println("Servidor recibió: " + rawJson);

        // Deserializar
        RpcRequest request = mapper.readValue(rawJson, RpcRequest.class);

        // Despachar (Ejecutar RPC)
        RpcResponse response = dispatcher.dispatch(request);

        // Serializar y responder
        String jsonResponse = mapper.writeValueAsString(response);
        out.println(jsonResponse);
        System.out.println("Servidor envió: " + jsonResponse);
      }
    } catch (Exception e) {
      System.err.println("Error de conexión: " + e.getMessage());
    }
  }
}
