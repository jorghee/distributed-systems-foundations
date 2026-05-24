import java.io.DataInputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClienteCristian {
    public static void main(String[] args) {
        String host = "localhost";
        int puerto = 8080;

        try {
            long tInicial = System.currentTimeMillis();
            Socket socket = new Socket(host, puerto);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            
            long tServidor = in.readLong();
            long tFinal = System.currentTimeMillis();
            socket.close();

            long rtt = tFinal - tInicial;
            long tiempoAjustado = tServidor + (rtt / 2);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            System.out.println("Hora del cliente al iniciar (T0): " + sdf.format(new Date(tInicial)));
            System.out.println("Hora recibida del servidor (Ts):  " + sdf.format(new Date(tServidor)));
            System.out.println("Tiempo de viaje ida y vuelta (RTT): " + rtt + " ms");
            System.out.println("--- HORA SINCRONIZADA ---:          " + sdf.format(new Date(tiempoAjustado)));

        } catch (Exception e) {
            System.out.println("Error en el cliente: " + e.getMessage());
        }
    }
}
