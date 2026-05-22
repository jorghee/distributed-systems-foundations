package converter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Scanner;

public class ClientMain {

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

    ConverterGrpc.ConverterBlockingStub stub = ConverterGrpc.newBlockingStub(channel);

    while (true) {
      System.out.println();
      System.out.println("=== SISTEMA DE CONVERSION ===");
      System.out.println("1. Celsius -> Fahrenheit");
      System.out.println("2. Soles -> Dólares");
      System.out.println("3. Kilómetros -> Millas");
      System.out.println("4. Kilogramos -> Libras");
      System.out.println("5. Metros -> Centímetros");
      System.out.println("6. Horas -> Minutos");
      System.out.println("0. Salir");
      System.out.print("Seleccione opción: ");

      int opcion = sc.nextInt();

      if (opcion == 0) {
        break;
      }

      String tipo = "";

      switch (opcion) {
        case 1:
          tipo = "c_f";
          System.out.print("Ingrese grados Celsius: ");
          break;
        case 2:
          tipo = "soles_dolares";
          System.out.print("Ingrese cantidad en soles: ");
          break;
        case 3:
          tipo = "km_millas";
          System.out.print("Ingrese kilómetros: ");
          break;
        case 4:
          tipo = "kg_lb";
          System.out.print("Ingrese kilogramos: ");
          break;
        case 5:
          tipo = "m_cm";
          System.out.print("Ingrese metros: ");
          break;
        case 6:
          tipo = "h_min";
          System.out.print("Ingrese horas: ");
          break;
        default:
          System.out.println("Opción inválida");
          continue;
      }

      double valor = sc.nextDouble();
      ConvertRequest request = ConvertRequest.newBuilder().setType(tipo).setValue(valor).build();
      ConvertResponse response = stub.convert(request);

      System.out.println("Resultado: " + response.getResult());
      System.out.println("Mensaje: " + response.getMessage());
    }

    channel.shutdown();
    sc.close();

    System.out.println("Programa finalizado");
  }
}
