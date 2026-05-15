import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Banco extends Remote {
    // Autenticación
    boolean iniciarSesion(String numeroTarjeta, String pin) throws RemoteException;
    
    // Operaciones
    double consultarSaldo(String numeroTarjeta) throws RemoteException;
    double consultarLimite(String numeroTarjeta) throws RemoteException;
    String realizarCompra(String numeroTarjeta, double monto) throws RemoteException;
    String abonarPago(String numeroTarjeta, double monto) throws RemoteException;
}