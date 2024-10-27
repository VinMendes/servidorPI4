import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

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

    // Função para inserir um documento na coleção
    public static void insertDocument(String jsonDocument, String colecao) throws Exception {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("fideliza");
            MongoCollection<Document> collection = database.getCollection(colecao);
            Document document = Document.parse(jsonDocument);
            collection.insertOne(document);
            System.out.println("Documento inserido com sucesso na colecao: " + colecao);
        } catch (MongoException e) {
            e.printStackTrace();
            throw new Exception("Erro ao conectar ou inserir o documento no MongoDB.", e);
        }
    }

    // Função para adicionar um ponto à pontuação de um cliente
    public static void adcPonto(String codigoPrograma, String cpf) {

        MongoClient mongoClient = null;

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("fideliza");
            MongoCollection<Document> collection = database.getCollection("pontos");
            MongoCollection<Document> collectionClientes = database.getCollection("clientes");

            Document ax = collectionClientes.find(Document.parse("{ \"cpf\" : \"" + cpf + "\" }")).first();
            String codigoCliente = ax.getObjectId("_id").toString();

            // Filtro para localizar o cliente e o programa de fidelidade
            Bson filtro = new Document("codigoPrograma", codigoPrograma)
                    .append("codigoCliente", codigoCliente);

            // Verifica se o documento já existe
            Document clientePonto = collection.find(filtro).first();

            if (clientePonto != null) {
                // Se o documento existir, incrementa a pontuação em 1
                collection.updateOne(filtro, Updates.inc("pontuacaoAtual", 1));
                System.out.println("Ponto adicionado para o cliente: " + codigoCliente);
            } else {
                // Se o documento não existir, cria um novo
                Document novoPonto = new Document("codigoPrograma", codigoPrograma)
                        .append("codigoCliente", codigoCliente)
                        .append("pontuacaoAtual", 1);
                collection.insertOne(novoPonto);
                System.out.println("Novo registro criado e ponto adicionado para o cliente: " + codigoCliente);
            }

        } catch (MongoException e) {
            System.err.println("Erro ao conectar ou atualizar o documento: " + e.getMessage());
        } finally {
            if (mongoClient != null) {
                try {
                    mongoClient.close();
                } catch (Exception e) {
                    System.err.println("Erro ao fechar a conexão com o MongoDB: " + e.getMessage());
                }
            }
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
                ret = "Nenhum documento encontrado.";
            }

            return ret;
        } catch (MongoException e) {
            System.err.println(e.getMessage());
            ret = e.getMessage();
        } finally {
            if (mongoClient != null) mongoClient.close();
        }

        return ret;
    }

    public static String buscarDetalhesProgramasPorFirebaseUID(String firebaseUID) {
        StringBuilder result = new StringBuilder();
        MongoClient mongoClient = null;
        Iterator<Document> iterator = null;

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("fideliza");

            // Obter Object ID do cliente
            MongoCollection<Document> clienteCollection = database.getCollection("clientes");
            Document clienteDoc = clienteCollection.find(new Document("firebaseUID", firebaseUID)).first();

            if (clienteDoc != null) {
                String codigoCliente = clienteDoc.getObjectId("_id").toString();

                // Obter dados a respeito dos pontos
                MongoCollection<Document> pontosCollection = database.getCollection("pontos");
                FindIterable<Document> pontosDocs = pontosCollection.find(new Document("codigoCliente", codigoCliente));
                iterator = pontosDocs.iterator();

                // Iterar sobre todos os pontos
                while (iterator.hasNext()) {
                    Document pontoDoc = iterator.next();
                    String codigoPrograma = pontoDoc.getString("codigoPrograma");
                    int pontuacaoAtual = pontoDoc.getInteger("pontuacaoAtual", 0);

                    // Buscar detalhes do programa
                    MongoCollection<Document> programasCollection = database.getCollection("programas");
                    Document programaDoc = programasCollection.find(new Document("_id", new ObjectId(codigoPrograma))).first();

                    if (programaDoc != null) {
                        String codigoEmpresa = programaDoc.getString("codigoEmpresa");
                        String descricaoPrograma = programaDoc.getString("descricaoDoPrograma");
                        int pontosNecessarios = programaDoc.getInteger("qtdDePontosNecessarios", 0);

                        // Buscar nome da empresa
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
                        }
                    }
                }
            } else {
                System.out.println("Cliente não encontrado com o firebaseUID: " + firebaseUID);
            }
        } catch (MongoException e) {
            System.err.println(e.getMessage());
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
}