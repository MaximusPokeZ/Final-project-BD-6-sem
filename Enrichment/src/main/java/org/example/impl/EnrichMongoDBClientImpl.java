package org.example.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import org.example.MongoDBClientEnricher;
import org.example.model.Quatrifollio;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class EnrichMongoDBClientImpl implements MongoDBClientEnricher {


    private final MongoClient mongoClient;

    private final MongoDatabase database;

    private MongoCollection<Document> collection;

    private final String nameCollection;


    public EnrichMongoDBClientImpl(Config config) {

        this.mongoClient = MongoClients.create(config.getString("mongo.connectionString"));
        this.database = mongoClient.getDatabase(config.getString("mongo.database"));
        this.nameCollection = config.getString("mongo.collection");
        this.collection = database.getCollection(nameCollection);
        insertSampleDocuments();
    }

    private void insertSampleDocuments() {
        List<Document> documents = new ArrayList<>();

        Document doc1 = new Document("status", "active")
                .append("createdAt", new Date());

        Document doc2 = new Document("status", "non-active")
                .append("createdAt", new Date());

        documents.add(doc1);
        documents.add(doc2);

        collection.insertMany(documents);
    }

    @Override
    public void enrichMessage(JsonNode messageInJson, Map<String, Quatrifollio<Long, String, String, String>> map) {

        log.info("start enriching message");

        collection = database.getCollection(nameCollection);

        for (Map.Entry<String, Quatrifollio<Long, String, String, String>> entry : map.entrySet()) {
            String fieldNameMongo = entry.getValue().fieldNameInMongo();
            String fieldValueMongo = entry.getValue().fieldValueInMongo();
            String fieldInMessage = entry.getKey();
            String defaultValue = entry.getValue().valueDefault();

            if ("state".equalsIgnoreCase(fieldInMessage) && "status".equalsIgnoreCase(fieldNameMongo) && ("active".equalsIgnoreCase(fieldValueMongo)|| "non-active".equalsIgnoreCase(fieldValueMongo))) {
                Double x1 = messageInJson.get("coordX1").asDouble();
                Double x2 = messageInJson.get("coordX2").asDouble();
                Document tmp;
                if (x1.equals(x2)) {
                    tmp = collection.find(Filters.eq(fieldNameMongo, "non-active")).sort(Sorts.descending("_id")).first();
                } else {
                    tmp = collection.find(Filters.eq(fieldNameMongo, "active")).first();
                }
                String statusValue = defaultValue;
                if (tmp != null) {
                    statusValue = tmp.getString("status");
                }
                ((ObjectNode) messageInJson).put(fieldInMessage, statusValue);

            } else {
                Document doc = collection.find(Filters.eq(fieldNameMongo, fieldValueMongo)).sort(Sorts.descending("_id")).first();

                if (doc != null) {

                    ObjectMapper objectMapper = new ObjectMapper();

                    ObjectNode documentNode = objectMapper.valueToTree(doc);

                    documentNode.remove("_id");

                    ObjectNode oidNode = objectMapper.createObjectNode();
                    oidNode.put("$oid", doc.get("_id").toString());

                    documentNode.set("_id", oidNode);

                    ((ObjectNode) messageInJson).set(fieldInMessage, documentNode);

                } else {
                    ((ObjectNode) messageInJson).put(fieldInMessage, defaultValue);
                }
            }
        }
    }
}
