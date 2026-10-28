package com.preproom.engine;

import java.time.Instant;
import java.util.Map;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DbTestController {

    private final MongoTemplate mongo;
    private final boolean enabled;

    public DbTestController(MongoTemplate mongo,
                            @Value("${preproom.dbtest.enabled:false}") boolean enabled) {
        this.mongo = mongo;
        this.enabled = enabled;
    }

    @GetMapping("/api/db-test")
    public Map<String, Object> test() {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var collection = mongo.getCollection("connection_test");
        collection.insertOne(new Document("note", "hello").append("at", Instant.now().toString()));
        return Map.of("inserted", true, "count", collection.countDocuments());
    }
}