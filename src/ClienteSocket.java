import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteSocket {
    public static void main(String[] args) {
        try {
            // Conecta ao servidor na porta 12345
            Socket socket = new Socket("localhost", 12345);
            System.out.println("Conectado ao servidor em " + socket.getRemoteSocketAddress());

            // Cria um BufferedReader para ler dados do console
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            // Cria um PrintWriter para enviar dados ao servidor
            PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);

            // Lê a mensagem digitada pelo usuário
            System.out.print("Digite a mensagem para enviar ao servidor: ");
            String mensagem = consoleReader.readLine();

            // Envia a mensagem ao servidor
            escritor.println(mensagem);
            System.out.println("Mensagem enviada ao servidor.");

            // Fecha os recursos
            escritor.close();
            consoleReader.close();
            socket.close();
            System.out.println("Conexão encerrada.");

        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        }
    }
}