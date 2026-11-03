package com.preproom.engine.session;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepository extends MongoRepository<SessionDoc, String> {}