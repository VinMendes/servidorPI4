import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class GerenciadorDeClientes extends Thread {

    private Socket cliente;

    GerenciadorDeClientes(Socket cliente) {
        this.cliente = cliente;
        start(); // Inicia a thread imediatamente
    }

    @Override
    public void run() {
        try {
            BufferedReader leitor = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            String linha = leitor.readLine();  // Lê apenas uma linha e encerra

            if (linha != null) {
                System.out.println("Recebido do cliente [" + cliente.getInetAddress() + "]: " + linha);

                // Extrair a coleção e o documento da string recebida
                String[] partes = linha.split(";", 2);
                if (partes.length == 2) {
                    String colecao = partes[0].replace("colecao=", "").trim();
                    String documento = partes[1].trim();

                    // Insere o documento na coleção especificada
                    MongoDB.insertDocument(documento, colecao);
                    System.out.println("Documento inserido na coleção: " + colecao);
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
                cliente.close();
            } catch (Exception e) {
                System.err.println("Erro ao fechar conexão com cliente: " + e.getMessage());
            }
        }
    }
}