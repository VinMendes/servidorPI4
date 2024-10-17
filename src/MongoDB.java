import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;

public class MongoDB {

    // **Importante**: Evite expor credenciais diretamente no código.
    // Considere utilizar variáveis de ambiente ou arquivos de configuração seguros.
    final static String connectionString = "mongodb+srv://viniciusmc6:051007@fidelizapi4.i1nd1.mongodb.net/?retryWrites=true&w=majority&appName=FidelizaPI4";

    private static final ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();

    private static final MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .serverApi(serverApi)
            .build();


    public static void insertDocument(String jsonDocument, String colecao) throws Exception {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            // Conectando ao banco de dados 'empresa'
            MongoDatabase database = mongoClient.getDatabase("fideliza");

            // Acessando a coleção 'empresas'
            MongoCollection<Document> collection = database.getCollection(colecao);

            // Convertendo o JSON para um documento BSON
            Document document = Document.parse(jsonDocument);

            // Inserindo o documento na coleção
            collection.insertOne(document);
        } catch (MongoException e) {
            e.printStackTrace();
            throw new Exception("Erro ao conectar ou inserir o documento no MongoDB.", e);
        }
    }

}