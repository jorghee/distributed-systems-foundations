import java.rmi.Naming;
import java.util.Scanner;

public class ClienteConversorMoneda {

  public static void main(String[] args) {
    try {
      ConversorMoneda conversor =
          (ConversorMoneda) Naming.lookup("rmi://localhost:1099/ConversorMonedaService");

      Scanner scanner = new Scanner(System.in);
      int opcion;

      do {
        System.out.println("\nMenu----------------------------------");
        System.out.println("1. Convertir soles a dolares");
        System.out.println("2. Convertir soles a euros");
        System.out.println("3. Salir");
        System.out.print("Seleccione una opcion: ");
        opcion = scanner.nextInt();

        procesarOpcion(opcion, scanner, conversor);

      } while (opcion != 3);

      scanner.close();

    } catch (Exception e) {
      System.out.println("Error en el cliente: " + e.getMessage());
    }
  }

  public static void procesarOpcion(int opcion, Scanner scanner, ConversorMoneda conversor) {
    try {
      switch (opcion) {
        case 1:
          System.out.print("Ingrese monto en soles: ");
          double montoDolares = scanner.nextDouble();

          double resultadoDolares = conversor.convertirADolares(montoDolares);

          System.out.printf("%.2f\n", resultadoDolares);
          break;

        case 2:
          System.out.print("Ingrese monto en soles: ");
          double montoEuros = scanner.nextDouble();

          double resultadoEuros = conversor.convertirAEuros(montoEuros);

          System.out.printf("%.2f\n", resultadoEuros);
          break;

        case 3:
          System.out.println("Saliendo del sistema");
          break;

        default:
          System.out.println("Opcion no valida");
          break;
      }
    } catch (Exception e) {
      System.out.println("Error al procesar la opcion: " + e.getMessage());
    }
  }
}

