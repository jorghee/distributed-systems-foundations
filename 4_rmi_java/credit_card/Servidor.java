import java.rmi.Naming;

public class Servidor {
    public static void main(String[] args) {
        try {
            BancoImpl banco = new BancoImpl();
            Naming.rebind("rmi://localhost/ServicioBancario", banco);
            
            System.out.println("=========================================");
            System.out.println("  SERVIDOR BANCARIO RMI EN EJECUCIÓN");
            System.out.println("=========================================");
            System.out.println("Tarjetas de prueba cargadas:");
            System.out.println("1) Num: 4500-1234-5678-9000 | PIN: 1234");
            System.out.println("2) Num: 5500-0000-1111-2222 | PIN: 9999");
            System.out.println("Esperando transacciones...");
        } catch (Exception e) {
            System.err.println("Error crítico en el servidor: " + e.getMessage());
        }
    }
}