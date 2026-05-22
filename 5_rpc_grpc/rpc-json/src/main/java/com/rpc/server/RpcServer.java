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
    System.out.println("Starting RPC Server on port " + PORT + "...");

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress());

        // Despacha el cliente a un hilo para manejar conexiones concurrentes
        threadPool.submit(() -> handleClient(clientSocket));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void handleClient(Socket socket) {
    // Try-with-resources asegura el cierre de streams y del socket
    try (socket;
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
      String rawJson;
      // Lee línea por línea (cada línea es un Request completo)
      while ((rawJson = in.readLine()) != null) {
        System.out.println("Server Received: " + rawJson);

        // 1. Deserializar
        RpcRequest request = mapper.readValue(rawJson, RpcRequest.class);

        // 2. Despachar (Ejecutar RPC)
        RpcResponse response = dispatcher.dispatch(request);

        // 3. Serializar y responder
        String jsonResponse = mapper.writeValueAsString(response);
        out.println(jsonResponse);
        System.out.println("Server Sent: " + jsonResponse);
      }
    } catch (Exception e) {
      System.err.println("Connection error: " + e.getMessage());
    }
  }
}
