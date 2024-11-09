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

        try {
            // Conecta ao servidor na porta 12345
            socket = new Socket("localhost", 12345);
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
            System.out.println("5 para buscar recompensas pelo firebaseUID");

            String operacao = consoleReader.readLine();

            // Declaração de variáveis específicas para cada campo
            String documentoJson = "";
            String colecaoNome = "";
            String codigoPrograma = "";
            String cpfCliente = "";
            String firebaseUID = "";

            String mensagem = null;

            // Solicita os dados baseados na operação
            if ("1".equals(operacao)) {
                System.out.print("Insira o documento em formato JSON: ");
                documentoJson = consoleReader.readLine();

                System.out.print("Insira o nome da coleção: ");
                colecaoNome = consoleReader.readLine();

                mensagem = operacao + ";" + documentoJson + ";" + colecaoNome;

            } else if ("2".equals(operacao)) {
                System.out.print("Insira o Firebase UID da empresa: ");
                firebaseUID = consoleReader.readLine();

                System.out.print("Insira o CPF do cliente: ");
                cpfCliente = consoleReader.readLine();

                System.out.print("Insira o documento JSON adicional para o histórico: ");
                documentoJson = consoleReader.readLine();

                mensagem = operacao + ";" + firebaseUID + ";" + cpfCliente + ";" + documentoJson;

            } else if ("3".equals(operacao)) {
                System.out.print("Insira o documento para busca: ");
                documentoJson = consoleReader.readLine();

                System.out.print("Insira a coleção: ");
                colecaoNome = consoleReader.readLine();

                mensagem = operacao + ";" + documentoJson + ";" + colecaoNome;

            } else if ("4".equals(operacao)) {
                System.out.print("Insira o firebaseUID da empresa: ");
                firebaseUID = consoleReader.readLine();

                mensagem = operacao + ";" + firebaseUID;

            } else if ("5".equals(operacao)) {
                System.out.println("Insira o firebaseUId do cliente: ");
                firebaseUID = consoleReader.readLine();

                mensagem = operacao + ";" + firebaseUID;
            } else {
                System.out.println("Operação inválida!");
                return;
            }

            // Envia os dados para o servidor no formato: operação;documento;colecao
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