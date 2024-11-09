import java.io.*;
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
            String linha = leitor.readLine();

            if (linha != null) {
                System.out.println("Recebido do cliente [" + cliente.getInetAddress() + "]: " + linha);

                // Extrair a operação, cliente e documento da string recebida
                String[] partes = linha.split(";", 4);
                if (partes.length > 0) {
                    String op = partes[0].trim();

                    switch (op) {
                        case "1":
                            if (partes.length >= 3) {
                                handleInsert(partes[1].trim(), partes[2].trim(), escritor);
                            } else {
                                escritor.write("Erro: Parâmetros insuficientes para inserção");
                                escritor.newLine();
                                escritor.flush();
                            }
                            break;
                        case "2":
                            if (partes.length >= 4) {
                                handleAddPoint(partes[1].trim(), partes[2].trim(), partes[3], escritor);
                            } else {
                                escritor.write("Erro: Parâmetros insuficientes para adicionar ponto");
                                escritor.newLine();
                                escritor.flush();
                            }
                            break;
                        case "3":
                            if (partes.length >= 3) {
                                handleBusca(partes[1].trim(), partes[2].trim(), escritor);
                            } else {
                                escritor.write("Erro: Parâmetros insuficientes para busca");
                                escritor.newLine();
                                escritor.flush();
                            }
                            break;
                        case "4":
                            if (partes.length >= 2) {
                                handleBuscaPontos(partes[1].trim(), escritor);
                            } else {
                                escritor.write("Erro: Parâmetros insuficientes para buscar pontos");
                                escritor.newLine();
                                escritor.flush();
                            }
                            break;
                        case "5":
                            if(partes.length >= 2) {
                                handleBuscaReconpensas(partes[1].trim(), escritor);
                            } else {
                                escritor.write("Erro: Parâmetros insuficientes para buscar pontos");
                                escritor.newLine();
                                escritor.flush();
                            }
                            break;
                        default:
                            escritor.write("Erro: Operação inválida");
                            escritor.newLine();
                            escritor.flush();
                    }
                } else {
                    System.err.println("Formato inválido recebido: " + linha);
                    escritor.write("Erro: Formato inválido");
                    escritor.newLine();
                    escritor.flush();
                }
            } else {
                System.out.println("Nenhuma mensagem recebida do cliente [" + cliente.getInetAddress() + "].");
            }
        } catch (Exception e) {
            try {
                escritor.write("Erro: " + e.getMessage());
                escritor.newLine();
                escritor.flush();
            } catch (IOException ioException) {
                System.err.println("Erro ao enviar mensagem de erro para o cliente: " + ioException.getMessage());
            }
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

    private static void handleInsert(String documento, String colecao, BufferedWriter escritor) throws IOException {
        try {
            MongoDB.insertDocument(documento, colecao);
            escritor.write("Sucesso");
            escritor.newLine();
            escritor.flush();
            System.out.println("Documento inserido com sucesso na coleção: " + colecao);
        } catch (Exception e) {
            escritor.write("Erro: " + e.getMessage()); // Inclui "Erro: " no início da mensagem
            escritor.newLine();
            escritor.flush();
        }
    }

    private static void handleAddPoint(String cliente, String programa, String doc, BufferedWriter escritor) throws IOException {
        try {
            MongoDB.adcPonto(cliente, programa, doc);
            escritor.write("Sucesso");
            escritor.newLine();
            escritor.flush();
            System.out.println("Ponto adicionado para o cliente: " + cliente);
        } catch (Exception e) {
            escritor.write("Erro: " + e.getMessage());
            escritor.newLine();
            escritor.flush();
        }
    }

    private static void handleBusca(String info, String colecao, BufferedWriter escritor) {
        try {
            String ret = MongoDB.buscarUmDocumento(info, colecao);
            escritor.write(ret);
            escritor.newLine();
            escritor.flush();
        } catch (Exception e) {
            try {
                escritor.write("Erro: " + e.getMessage());
                escritor.newLine();
                escritor.flush();
            } catch (IOException ioException) {
                System.err.println("Erro ao enviar mensagem de erro para o cliente: " + ioException.getMessage());
            }
        }
    }

    private static void handleBuscaPontos(String firebaseUID, BufferedWriter escritor) {
        try {
            String resultado = MongoDB.buscarDetalhesProgramasPorFirebaseUID(firebaseUID);
            escritor.write(resultado);
            escritor.newLine();
            escritor.flush();
        } catch (Exception e) {
            try {
                escritor.write("Erro: " + e.getMessage());
                escritor.newLine();
                escritor.flush();
            } catch (IOException ioException) {
                System.err.println("Erro ao enviar mensagem de erro para o cliente: " + ioException.getMessage());
            }
        }
    }

    private static void handleBuscaReconpensas(String firebaseUID, BufferedWriter escritor) {
        try {
            String resultado = MongoDB.BuscaDetalhadaRecompensas(firebaseUID);
            escritor.write(resultado);
            escritor.newLine();
            escritor.flush();
        } catch (Exception e) {
            try {
                escritor.write("Erro: " + e.getMessage());
                escritor.newLine();
                escritor.flush();
            } catch (IOException ioException) {
                System.err.println("Erro ao enviar mensagem de erro para o cliente: " + ioException.getMessage());
            }
        }
    }
}