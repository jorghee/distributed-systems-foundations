import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ConversorMonedaImpl extends UnicastRemoteObject implements ConversorMoneda {

  private static final double T_DOLAR = 3.75;
  private static final double T_EURO = 4.05;

  public ConversorMonedaImpl() throws RemoteException {
    super();
  }

  @Override
  public double convertirADolares(double monto) throws RemoteException {
    return monto / T_DOLAR;
  }

  @Override
  public double convertirAEuros(double monto) throws RemoteException {
    return monto / T_EURO;
  }
}

