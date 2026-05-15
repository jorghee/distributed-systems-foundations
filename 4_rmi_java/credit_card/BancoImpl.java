import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class BancoImpl extends UnicastRemoteObject implements Banco {
    
    // Clase interna para simular el registro en la base de datos
    private class Tarjeta {
        String pin;
        double deuda;
        double limite;

        Tarjeta(String pin, double limite) {
            this.pin = pin;
            this.limite = limite;
            this.deuda = 0.0;
        }
    }

    // Nuestra "Base de datos" en memoria
    private Map<String, Tarjeta> baseDeDatos;

    public BancoImpl() throws RemoteException {
        super();
        baseDeDatos = new HashMap<>();
        // Precargamos un par de tarjetas para rmicprobar
        baseDeDatos.put("4500-1234-5678-9000", new Tarjeta("1234", 5000.0)); // Tarjeta Clásica
        baseDeDatos.put("5500-0000-1111-2222", new Tarjeta("9999", 25000.0)); // Tarjeta Platinum
    }

    @Override
    public boolean iniciarSesion(String numeroTarjeta, String pin) throws RemoteException {
        if (baseDeDatos.containsKey(numeroTarjeta)) {
            return baseDeDatos.get(numeroTarjeta).pin.equals(pin);
        }
        return false;
    }

    @Override
    public double consultarSaldo(String numeroTarjeta) throws RemoteException {
        return baseDeDatos.get(numeroTarjeta).deuda;
    }

    @Override
    public double consultarLimite(String numeroTarjeta) throws RemoteException {
        return baseDeDatos.get(numeroTarjeta).limite;
    }

    @Override
    public String realizarCompra(String numeroTarjeta, double monto) throws RemoteException {
        Tarjeta t = baseDeDatos.get(numeroTarjeta);
        if (monto <= 0) return "El monto de compra debe ser positivo.";
        
        if (t.deuda + monto <= t.limite) {
            t.deuda += monto;
            return "Compra aprobada por S/. " + monto;
        } else {
            return "Compra rechazada: Límite de crédito excedido. Disponible: S/. " + (t.limite - t.deuda);
        }
    }

    @Override
    public String abonarPago(String numeroTarjeta, double monto) throws RemoteException {
        Tarjeta t = baseDeDatos.get(numeroTarjeta);
        if (monto <= 0) return "El monto del pago debe ser positivo.";
        
        t.deuda -= monto; // Permite saldo a favor (deuda negativa) como en la vida real
        return "Pago procesado exitosamente por S/. " + monto;
    }
}