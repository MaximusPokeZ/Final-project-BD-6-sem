package org.example;


import com.fasterxml.jackson.databind.JsonNode;
import org.example.model.Quatrifollio;


import java.util.Map;

public interface MongoDBClientEnricher {
    void enrichMessage(JsonNode messageInJson, Map<String, Quatrifollio<Long, String, String, String>> map);
}
