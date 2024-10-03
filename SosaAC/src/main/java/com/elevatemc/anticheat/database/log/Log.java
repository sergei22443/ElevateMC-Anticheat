package com.elevatemc.anticheat.database.log;

import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

@Getter
public class Log {
    private final long timestamp;

    private final UUID uuid;
    private final String check, type, data;

    private final double vl;
    private final int ping;
    private final String brand;

    public Log(long timestamp, UUID uuid, String check, String type, String data, double vl, int ping, String brand) {
        this.timestamp = timestamp;

        this.uuid = uuid;
        this.check = check;
        this.type = type;
        this.data = data;
        this.vl = vl;
        this.ping = ping;
        this.brand = brand;
    }

    public Log(UUID uuid, String check, String type, String data, double vl, int ping, String brand) {
        this.timestamp = System.currentTimeMillis();

        this.uuid = uuid;
        this.check = check;
        this.type = type;
        this.data = data;
        this.vl = vl;
        this.ping = ping;
        this.brand = brand;
    }

    public static Document toDocument(Log log) {
        Document document = new Document();

        document.put("time", log.getTimestamp());
        document.put("uuid", log.getUuid().toString());
        document.put("check", log.getCheck());
        document.put("type", log.getType());
        document.put("data", log.getData());
        document.put("vl", log.getVl());
        document.put("ping", log.getPing());
        document.put("brand", log.getBrand());

        return document;
    }

    public static Log fromDocument(Document document) {
        return new Log(
                document.getLong("time"), UUID.fromString(document.getString("uuid")),
                document.getString("check"), document.getString("type"),
                document.getString("data"), document.getDouble("vl"),
                document.getInteger("ping",0), document.getString("brand")
        );
    }
}