package com.preproom.engine.jd;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("jd_cache")
public record JdCacheEntry(@Id String id, Instant createdAt, JobDescription jd, List<String> rejected) {}