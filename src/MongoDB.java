import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Vector;

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
    public static void adcPonto(String codigoPrograma, String codigoCliente) {

        MongoClient mongoClient = null;

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("fideliza");
            MongoCollection<Document> collection = database.getCollection("pontos");

            // Filtro para localizar o cliente e o programa de fidelidade
            Bson filtro = new Document("codigoPrograma", codigoPrograma)
                    .append("codigoCliente", codigoCliente);

            // Verifica se o documento já existe
            Document clientePonto = collection.find(filtro).first();

            if (clientePonto != null) {
                // Se o documento existir, incrementa a pontuação em 1
                collection.updateOne(filtro, Updates.inc("pontos", 1));
                System.out.println("Ponto adicionado para o cliente: " + codigoCliente);
            } else {
                // Se o documento não existir, cria um novo
                Document novoPonto = new Document("codigoPrograma", codigoPrograma)
                        .append("codigoCliente", codigoCliente)
                        .append("pontos", 1);
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

    public static String getInfoCliente(String infoBusca, String colecao) {

        MongoClient mongoClient = null;
        String ret = null;

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("fideliza");
            MongoCollection<Document> collection = database.getCollection(colecao);

            // Realiza a busca e converte os resultados em String
            FindIterable<Document> iterable = collection.find(Document.parse(infoBusca));
            StringBuilder result = new StringBuilder();
            for (Document doc : iterable) {
                result.append(doc.toJson()).append("\n"); // Converte o documento para JSON e adiciona à String
            }

            ret = result.toString();
            return ret;
        } catch (MongoException e) {
            System.err.println(e.getMessage());
            ret = e.getMessage();
        } finally {
            if (mongoClient != null) mongoClient.close();
        }
        return ret;
    }
}