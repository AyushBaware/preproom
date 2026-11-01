package com.preproom.engine.jd;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface JdCacheRepository extends MongoRepository<JdCacheEntry, String> {}