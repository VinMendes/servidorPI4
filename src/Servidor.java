import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(12345);
            System.out.println("Servidor iniciado na porta 12345.");

            while (true) {
                Socket socket = server.accept();
                System.out.println("\n\nNovo cliente conectado: " + socket.getInetAddress());
                GerenciadorDeClientes conecao = new GerenciadorDeClientes(socket);
            }

        } catch (Exception err) {
            System.err.println(err.getMessage());
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}