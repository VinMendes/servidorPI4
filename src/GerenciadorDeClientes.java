import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class GerenciadorDeClientes extends Thread {

    private final Socket cliente;

    GerenciadorDeClientes(Socket cliente) {
        this.cliente = cliente;
        start();
    }

    @Override
    public void run() {
        BufferedReader leitor = null;
        BufferedWriter escritor = null;

        try {
            leitor = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            escritor = new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream()));
            String linha = leitor.readLine();  // Lê apenas uma linha e encerra

            if (linha != null) {
                System.out.println("Recebido do cliente [" + cliente.getInetAddress() + "]: " + linha);

                // Extrair a operação, cliente e documento da string recebida
                String[] partes = linha.split(";", 3);
                if (partes.length == 3) {
                    String op = partes[0].trim();

                    if (op.equals("1")) {
                        handleInsert(partes[1].trim(), partes[2].trim(), escritor);
                    } else {
                        handleAddPoint(partes[1].trim(), partes[2].trim(), escritor);
                    }
                } else {
                    System.err.println("Formato inválido recebido: " + linha);
                }
            } else {
                System.out.println("Nenhuma mensagem recebida do cliente [" + cliente.getInetAddress() + "].");
            }
        } catch (Exception e) {
            System.err.println("Erro ao gerenciar cliente: " + e.getMessage());
        } finally {
            try {
                if (leitor != null) leitor.close();
                if (escritor != null) escritor.close();
                cliente.close();
            } catch (Exception e) {
                System.err.println("Erro ao fechar recursos ou conexão com cliente: " + e.getMessage());
            }
        }
    }

    private void handleInsert(String documento, String colecao, BufferedWriter escritor) {
        try {
            MongoDB.insertDocument(documento, colecao);
            escritor.write("Sucesso ao inserir documento: " + documento);
            escritor.newLine();
            escritor.flush();
        } catch (Exception e) {
            handleException(escritor, "Erro ao inserir documento", e);
        }
    }

    private void handleAddPoint(String cliente, String programa, BufferedWriter escritor) {
        try {
            MongoDB.adcPonto(cliente, programa);
            escritor.write("Sucesso ao adicionar ponto ao cliente: " + programa);
            escritor.newLine();
            escritor.flush();
        } catch (Exception e) {
            handleException(escritor, "Erro ao adicionar ponto", e);
        }
    }

    private void handleException(BufferedWriter escritor, String message, Exception e) {
        try {
            escritor.write(message + ": " + e.getMessage());
            escritor.newLine();
            escritor.flush();
        } catch (Exception ex) {
            System.err.println("Erro ao enviar mensagem de erro para o cliente: " + ex.getMessage());
        }
    }
}