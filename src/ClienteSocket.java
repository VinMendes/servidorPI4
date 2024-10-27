import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClienteSocket {

    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader consoleReader = null;
        BufferedReader servidorReader = null;
        BufferedWriter servidorWriter = null;
        String mensagem = null;

        try {
            // Conecta ao servidor na porta 12345
            socket = new Socket("54.94.21.251", 12345);
            System.out.println("Conectado ao servidor: " + socket.getRemoteSocketAddress());

            // Prepara leitores e escritores para o console e o servidor
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
            servidorReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            servidorWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Solicita o tipo de operação
            System.out.println("Digite o número da operação: ");
            System.out.println("1 para inserir documento");
            System.out.println("2 para adicionar ponto");
            System.out.println("3 para consultar um documento");
            System.out.println("4 para buscar programas de fidelidade pelo firebaseUID");

            String operacao = consoleReader.readLine();

            // Solicita os dados baseados na operação
            String documentoOuCliente = "";
            String colecaoOuPrograma = "";

            if ("1".equals(operacao)) {
                System.out.print("Insira o documento em formato JSON: ");
                documentoOuCliente = consoleReader.readLine();

                System.out.print("Insira o nome da coleção: ");
                colecaoOuPrograma = consoleReader.readLine();

                mensagem = operacao + ";" + documentoOuCliente + ";" + colecaoOuPrograma;

            } else if ("2".equals(operacao)) {
                System.out.print("Insira o código do programa ");
                documentoOuCliente = consoleReader.readLine();

                System.out.print("Insira o CPF: ");
                colecaoOuPrograma = consoleReader.readLine();

                mensagem = operacao + ";" + documentoOuCliente + ";" + colecaoOuPrograma;

            } else if ("3".equals(operacao)) {
                System.out.print("Insira o documento para busca: ");
                documentoOuCliente = consoleReader.readLine();

                System.out.print("Insira a coleção: ");
                colecaoOuPrograma = consoleReader.readLine();

                mensagem = operacao + ";" + documentoOuCliente + ";" + colecaoOuPrograma;

            } else if ("4".equals(operacao)) {
                System.out.print("Insira o firebaseUID do cliente: ");
                documentoOuCliente = consoleReader.readLine();

                mensagem = operacao + ";" + documentoOuCliente;

            } else {
                System.out.println("Operação inválida!");
                return;
            }

            // Envia os dados para o servidor no formato: operação;documentoOuCliente;colecaoOuPrograma
            servidorWriter.write(mensagem);
            servidorWriter.newLine();
            servidorWriter.flush();

            // Aguarda a resposta do servidor
            String resposta = servidorReader.readLine();
            System.out.println("\nResposta do servidor: " + resposta);

        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        } finally {
            try {
                if (consoleReader != null) consoleReader.close();
                if (servidorReader != null) servidorReader.close();
                if (servidorWriter != null) servidorWriter.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                System.err.println("Erro ao fechar recursos do cliente: " + e.getMessage());
            }
        }
    }
}