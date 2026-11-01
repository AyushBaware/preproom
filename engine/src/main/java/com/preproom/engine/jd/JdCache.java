package com.preproom.engine.jd;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Remembers verified JD extractions so the same document is analysed only once. */
@Component
public class JdCache {

    private static final Logger log = LoggerFactory.getLogger(JdCache.class);

    private final JdCacheRepository repository;

    public JdCache(JdCacheRepository repository) {
        this.repository = repository;
    }

    public Optional<ExtractionResult> find(String documentText) {
        try {
            return repository.findById(keyFor(documentText))
                    .map(entry -> new ExtractionResult(entry.jd(), entry.rejected()));
        } catch (RuntimeException e) {
            log.warn("JD cache lookup failed, continuing without the cache", e);
            return Optional.empty();
        }
    }

    public void save(String documentText, ExtractionResult result) {
        try {
            repository.save(new JdCacheEntry(
                    keyFor(documentText), Instant.now(), withSafeKeys(result.jd()), result.rejected()));
        } catch (RuntimeException e) {
            log.warn("JD cache save failed, continuing without the cache", e);
        }
    }

    static String keyFor(String documentText) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(QuoteVerifier.fingerprint(documentText).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The AI chooses the keys of otherDetails. MongoDB map keys must not contain dots or dollar signs. */
    static JobDescription withSafeKeys(JobDescription jd) {
        Map<String, String> safe = null;
        if (jd.otherDetails() != null) {
            safe = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : jd.otherDetails().entrySet()) {
                String key = (e.getKey() == null || e.getKey().isBlank())
                        ? "field" : e.getKey().replace('.', '_').replace('$', '_');
                safe.put(key, e.getValue());
            }
        }
        return new JobDescription(jd.company(), jd.roleTitle(), jd.employmentType(), jd.location(),
                jd.workMode(), jd.requiredSkills(), jd.preferredSkills(), jd.responsibilities(),
                jd.softTraits(), jd.applicationProcess(), jd.logistics(), safe);
    }
}