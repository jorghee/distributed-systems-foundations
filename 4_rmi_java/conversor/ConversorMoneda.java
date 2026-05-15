import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ConversorMoneda extends Remote {

  double convertirADolares(double monto) throws RemoteException;

  double convertirAEuros(double monto) throws RemoteException;
}

