import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Iterator;

public class MongoDB {

    // **Importante**: Evite expor credenciais diretamente no código.
    // Considere utilizar variáveis de ambiente ou arquivos de configuração seguros.
    final static String connectionString = "mongodb+srv://ServidorAWS:123456qwert@fidelizapi4.i1nd1.mongodb.net/?retryWrites=true&w=majority&appName=FidelizaPI4";

    private static final ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();

    private static final MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .serverApi(serverApi)
            .build();

    public static void insertDocument(String jsonDocument, String colecao) throws MongoException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("fideliza");
            MongoCollection<Document> collection = database.getCollection(colecao);
            Document document = Document.parse(jsonDocument);
            collection.insertOne(document);
        } catch (MongoException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void adcPonto(String firebaseUID, String cpf, String jsonDoc) throws Exception {
        MongoClient mongoClient = null;

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("fideliza");

            // Buscar o ObjectId da empresa usando o firebaseUID
            MongoCollection<Document> empresasCollection = database.getCollection("empresas");
            Document empresaDoc = empresasCollection.find(Document.parse("{ \"firebaseUID\" : \"" + firebaseUID + "\" }")).first();
            if (empresaDoc == null) {
                throw new Exception("Empresa não encontrada com o firebaseUID: " + firebaseUID);
            }
            ObjectId empresaId = empresaDoc.getObjectId("_id");

            // Buscar o ObjectId do cliente usando o CPF
            MongoCollection<Document> clientesCollection = database.getCollection("clientes");
            Document clienteDoc = clientesCollection.find(Document.parse("{ \"cpf\" : \"" + cpf + "\" }")).first();
            if (clienteDoc == null) {
                throw new Exception("Cliente não encontrado com o CPF: " + cpf);
            }
            ObjectId clienteId = clienteDoc.getObjectId("_id");

            // Construir o filtro para busca na coleção de pontos usando Document
            Document filter = new Document()
                    .append("codigoCliente", clienteId.toHexString())
                    .append("codigoEmpresa", empresaId.toHexString());

            MongoCollection<Document> pontosCollection = database.getCollection("pontos");
            Document pontoDoc = pontosCollection.find(filter).first();

            int pontosNecessarios = 5; // Defina o valor conforme o seu sistema de recompensas

            if (pontoDoc == null) {
                // Se não encontrar, cria um novo documento de pontos com pontuacaoAtual = 1
                Document novoPontoDoc = new Document()
                        .append("codigoCliente", clienteId.toHexString())
                        .append("codigoEmpresa", empresaId.toHexString())
                        .append("pontuacaoAtual", 1);
                pontosCollection.insertOne(novoPontoDoc);
                System.out.println("Novo documento de pontos criado com pontuacaoAtual = 1.");
            } else {
                int pontuacaoAtual = pontoDoc.getInteger("pontuacaoAtual");

                // Verifica se a pontuação atual menos os pontos necessários é igual a 1
                if ((pontosNecessarios - pontuacaoAtual) == 1) {
                    // Deleta o documento e cria um novo na coleção recompensas
                    pontosCollection.deleteOne(filter);

                    MongoCollection<Document> recompensasCollection = database.getCollection("recompensas");
                    Document recompensaDoc = new Document()
                            .append("codigoEmpresa", empresaId.toHexString())
                            .append("codigoCliente", clienteId.toHexString())
                            .append("data", new Date());
                    recompensasCollection.insertOne(recompensaDoc);
                    System.out.println("Recompensa criada com sucesso.");
                } else {
                    // Se a pontuação atual não está próxima dos pontos necessários, incrementa a pontuação
                    Document update = new Document("$inc", new Document("pontuacaoAtual", 1));
                    pontosCollection.findOneAndUpdate(filter, update);
                    System.out.println("Pontuação atualizada com sucesso para o cliente.");
                }
            }

            // Inserir os dados no histórico
            MongoCollection<Document> historicoCollection = database.getCollection("historico");
            Document historicoDoc = Document.parse(jsonDoc);
            historicoDoc.append("codigoCliente", clienteId)
                    .append("codigoEmpresa", empresaId)
                    .append("data", new Date());

            historicoCollection.insertOne(historicoDoc);
            System.out.println("Registro adicionado ao histórico com sucesso.");

        } catch (MongoException e) {
            e.printStackTrace();  // Log do erro no servidor
            throw e;  // Propaga o erro para ser capturado no chamador
        } finally {
            if (mongoClient != null) mongoClient.close();
        }
    }

    public static String buscarUmDocumento(String infoBusca, String colecao) {
        MongoClient mongoClient = null;
        String ret = null;

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("fideliza");
            MongoCollection<Document> collection = database.getCollection(colecao);

            // Busca um único documento correspondente
            Document doc = collection.find(Document.parse(infoBusca)).first();

            if (doc != null) {
                ret = doc.toJson(); // Converte o documento para JSON
            } else {
                ret = "nenhum documento encontrado.";
            }

            return ret;
        } catch (MongoException e) {
            System.err.println(e.getMessage()); // Log do erro no servidor
            throw e; // Propaga o erro para que o chamador possa tratá-lo
        } finally {
            if (mongoClient != null) mongoClient.close(); // Fecha o cliente para liberar recursos
        }
    }

    public static String buscarDetalhesProgramasPorFirebaseUID(String firebaseUID) {
        StringBuilder result = new StringBuilder();
        MongoClient mongoClient = null;

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("fideliza");

            // Obter Object ID do cliente
            MongoCollection<Document> clienteCollection = database.getCollection("clientes");
            Document clienteDoc = clienteCollection.find(new Document("firebaseUID", firebaseUID)).first();

            if (clienteDoc != null) {
                String codigoCliente = clienteDoc.getObjectId("_id").toString();
                System.out.println("Cliente encontrado: " + clienteDoc.toJson());

                // Obter o único programa
                MongoCollection<Document> programasCollection = database.getCollection("programas");
                Document programaDoc = programasCollection.find().first(); // Assume que há apenas um programa

                if (programaDoc != null) {
                    String descricaoPrograma = programaDoc.getString("descricaoDoPrograma");
                    int pontosNecessarios = programaDoc.getInteger("qtdDePontosNecessarios", 0);

                    // Obter dados a respeito dos pontos para cada empresa
                    MongoCollection<Document> pontosCollection = database.getCollection("pontos");
                    FindIterable<Document> pontosDocs = pontosCollection.find(new Document("codigoCliente", codigoCliente));

                    for (Document pontoDoc : pontosDocs) {
                        String codigoEmpresa = pontoDoc.getString("codigoEmpresa");
                        int pontuacaoAtual = pontoDoc.getInteger("pontuacaoAtual", 0);

                        // Buscar o nome da empresa
                        MongoCollection<Document> empresasCollection = database.getCollection("empresas");
                        Document empresaDoc = empresasCollection.find(new Document("_id", new ObjectId(codigoEmpresa))).first();

                        if (empresaDoc != null) {
                            String nomeEmpresa = empresaDoc.getString("nome");

                            // Montar o resultado em um documento JSON e adicionar ao StringBuilder
                            Document retDoc = new Document();
                            retDoc.put("empresa", nomeEmpresa);
                            retDoc.put("descricao", descricaoPrograma);
                            retDoc.put("pontosNecessarios", pontosNecessarios);
                            retDoc.put("pontuacaoAtual", pontuacaoAtual);

                            result.append(retDoc.toJson()).append(";");
                        } else {
                            System.err.println("Erro: Empresa não encontrada para o código fornecido.");
                        }
                    }
                } else {
                    System.err.println("Erro: Nenhum programa encontrado.");
                }
            } else {
                System.out.println("Cliente não encontrado com o firebaseUID: " + firebaseUID);
            }
        } catch (MongoException e) {
            System.err.println("Erro ao buscar detalhes dos programas: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }

        // Verificar se há resultados
        if (result.length() > 0) {
            return result.toString();
        } else {
            return "Nenhum programa encontrado.";
        }
    }

    public static String BuscaDetalhadaRecompensas(String firebaseUID) {

        StringBuilder result = new StringBuilder();
        MongoClient mongoClient = null;

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("fideliza");

            // Obter Object ID do cliente
            MongoCollection<Document> clienteCollection = database.getCollection("clientes");
            Document clienteDoc = clienteCollection.find(new Document("firebaseUID", firebaseUID)).first();

            if (clienteDoc != null) {
                ObjectId codigoCliente = clienteDoc.getObjectId("_id");
                System.out.println("Cliente encontrado: " + clienteDoc.toJson());
                System.out.println("codigoCliente (ObjectId): " + codigoCliente);

                // Obter o único programa
                MongoCollection<Document> programasCollection = database.getCollection("programas");
                Document programaDoc = programasCollection.find().first();

                if (programaDoc != null) {
                    String descricaoPrograma = programaDoc.getString("descricaoDoPrograma");

                    // Obter dados a respeito de cada recompensa
                    MongoCollection<Document> recompensaCollection = database.getCollection("recompensas");
                    FindIterable<Document> recompensaDocs = recompensaCollection.find(new Document("codigoCliente", codigoCliente.toHexString()));

                    for (Document recompensa : recompensaDocs) {
                        System.out.println("Recompensa encontrada: " + recompensa.toJson());

                        String codigoEmpresa = recompensa.getString("codigoEmpresa");
                        Date data = recompensa.getDate("data");

                        MongoCollection<Document> empresasCollection = database.getCollection("empresas");
                        Document empresaDoc = empresasCollection.find(new Document("_id", new ObjectId(codigoEmpresa))).first();

                        if (empresaDoc != null) {
                            String nomeEmpresa = empresaDoc.getString("nome");

                            // Montar o resultado em um documento JSON e adicionar ao StringBuilder
                            Document retDoc = new Document();
                            retDoc.put("empresa", nomeEmpresa);
                            retDoc.put("descricao", descricaoPrograma);
                            retDoc.put("data", data);

                            result.append(retDoc.toJson()).append(";");
                        } else {
                            System.err.println("Erro: Empresa não encontrada para o código fornecido.");
                        }
                    }

                } else {
                    System.out.println("Programa não encontrado");
                    result.append("Programa não encontrado");
                }
            } else {
                System.out.println("Cliente não encontrado com o firebaseUID");
                result.append("Cliente não encontrado com o firebaseUID");
            }

        } catch (Exception e) {
            System.err.println("Erro ao buscar recompensas: " + e.getMessage());
            result.append("Erro ao buscar recompensas: ").append(e.getMessage());
        } finally {
            if (mongoClient != null) mongoClient.close();
        }

        if(result.length() < 1) {
            return "Nenhum documento encontrado";
        }

        return result.toString();
    }
}
