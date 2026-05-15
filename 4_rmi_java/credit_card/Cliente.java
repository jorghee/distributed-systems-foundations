import java.rmi.Naming;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            Banco banco = (Banco) Naming.lookup("rmi://localhost/ServicioBancario");
            System.out.println("Conexión establecida con el servidor bancario.\n");

            while (true) {
                System.out.println("=== TERMINAL DE TARJETA DE CRÉDITO ===");
                System.out.print("Ingrese número de tarjeta (o 'salir' para terminar): ");
                String tarjeta = scanner.nextLine();

                if (tarjeta.equalsIgnoreCase("salir")) {
                    System.out.println("Apagando terminal. ¡Hasta luego!");
                    break;
                }

                System.out.print("Ingrese PIN: ");
                String pin = scanner.nextLine();

                System.out.println("Verificando credenciales...");
                if (banco.iniciarSesion(tarjeta, pin)) {
                    menuSesion(scanner, banco, tarjeta);
                } else {
                    System.out.println("ERROR: Número de tarjeta o PIN incorrecto.\n");
                }
            }
        } catch (Exception e) {
            System.err.println("Error de conexión: No se pudo conectar con el banco.");
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void menuSesion(Scanner scanner, Banco banco, String tarjeta) throws Exception {
        boolean sesionActiva = true;
        while (sesionActiva) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Consultar Deuda Actual");
            System.out.println("2. Consultar Crédito Disponible");
            System.out.println("3. Realizar una Compra");
            System.out.println("4. Pagar Tarjeta");
            System.out.println("5. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");
            
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    double deuda = banco.consultarSaldo(tarjeta);
                    System.out.println("\n==> Su deuda actual es: S/. " + deuda);
                    break;
                case "2":
                    double lim = banco.consultarLimite(tarjeta);
                    double d = banco.consultarSaldo(tarjeta);
                    System.out.println("\n==> Su límite total es: S/. " + lim);
                    System.out.println("==> Crédito disponible para compras: S/. " + (lim - d));
                    break;
                case "3":
                    System.out.print("\nIngrese el monto de la compra: S/. ");
                    double compra = Double.parseDouble(scanner.nextLine());
                    System.out.println(banco.realizarCompra(tarjeta, compra));
                    break;
                case "4":
                    System.out.print("\nIngrese el monto a pagar: S/. ");
                    double pago = Double.parseDouble(scanner.nextLine());
                    System.out.println(banco.abonarPago(tarjeta, pago));
                    break;
                case "5":
                    System.out.println("\nCerrando sesión de forma segura...");
                    sesionActiva = false;
                    break;
                default:
                    System.out.println("\nOpción no válida. Intente de nuevo.");
            }
        }
    }
}