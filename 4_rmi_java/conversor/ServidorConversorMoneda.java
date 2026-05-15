import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServidorConversorMoneda {

  public static void main(String[] args) {
    try {
      LocateRegistry.createRegistry(1099);

      ConversorMoneda conversor = new ConversorMonedaImpl();

      Naming.rebind("rmi://localhost:1099/ConversorMonedaService", conversor);

      System.out.println("Servidor listo en rmi://localhost:1099/ConversorMonedaService");
    } catch (Exception e) {
      System.out.println("Error en el servidor: " + e.getMessage());
    }
  }
}

