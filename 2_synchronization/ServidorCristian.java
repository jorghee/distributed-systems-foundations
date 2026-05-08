import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorCristian {
    public static void main(String[] args) {
        int puerto = 8080;
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor de tiempo iniciado en el puerto " + puerto + ". Esperando clientes...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado.");
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                long tiempoServidor = System.currentTimeMillis();
                out.writeLong(tiempoServidor);
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
}
