package fr.olympa.api.common.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MongoManager {

    private MongoClient client;
    private MongoDatabase database;

    public MongoManager(MongoServerInfo serverInfo){
        String serverUri = "mongodb://{username}:{password}@{host}:{port}/?authSource=admin"
                .replace("{host}", serverInfo.getHost())
                .replace("{port}", Integer.toString(serverInfo.getPort()))
                .replace("{username}", serverInfo.getUsername())
                .replace("{password}", serverInfo.getPassword());

        client = new MongoClient(new MongoClientURI(serverUri));
        database = client.getDatabase("olympa");
    }

    public MongoDatabase getDatabase() {
        return database;
    }
}
