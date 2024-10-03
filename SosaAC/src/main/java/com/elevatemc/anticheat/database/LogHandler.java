package com.elevatemc.anticheat.database;

import com.elevatemc.anticheat.SosaPlugin;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

import com.mongodb.client.result.UpdateResult;

public class LogHandler {

    private static final String MONGO_COLLECTION = "PlayerViolations";

    private static MongoDatabase db;
    private static MongoClient mongoPool = new MongoClient();

    @Getter
    private static final boolean loading = false;

    public LogHandler() {
        FileConfiguration config = SosaPlugin.getInstance().getConfig();
        String database = config.getString("database-logs.database");
        String host = config.getString("database-logs.host");
        int port = config.getInt("database-logs.port");
        String password = config.getString("database-logs.password");
        String username = config.getString("database-logs.username");

        if (password.isEmpty()) {
            mongoPool = new MongoClient(host, port);
        } else {
            mongoPool = new MongoClient(
                    new ServerAddress(host, port),
                    MongoCredential.createCredential(username, database, password.toCharArray()),
                    MongoClientOptions.builder().build()
            );
        }

        if (!database.isEmpty()) {
            db = mongoPool.getDatabase(database);
        }
    }

    public void closeMongo() {
        mongoPool.close();
    }

    public Document getPlayer(UUID uuid) {
        return getCollection(MONGO_COLLECTION).find(Filters.eq("uuid", uuid.toString())).first();
    }

    public MongoCollection<Document> getCollection(String coll) {
        if (db == null) {
            db = mongoPool.getDatabase(SosaPlugin.getInstance().getName());
        }
        return db.getCollection(coll);
    }

}