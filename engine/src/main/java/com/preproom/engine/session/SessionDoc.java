package com.preproom.engine.session;

import com.preproom.engine.gap.GapReport;
import com.preproom.engine.interview.InterviewKit;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sessions")
public record SessionDoc(@Id String id, Instant createdAt, String roleTitle, GapReport gap,
                         List<InterviewKit.KitQuestion> questions, List<Attempt> attempts) {

    public record Attempt(int questionId, Instant at, String status, Integer score, List<PointOutcome> points) {}

    public record PointOutcome(int index, String point, String coverage, String feedback) {}

    public SessionDoc withAttempt(Attempt attempt) {
        List<Attempt> updated = new ArrayList<>(attempts == null ? List.of() : attempts);
        updated.add(attempt);
        return new SessionDoc(id, createdAt, roleTitle, gap, questions, updated);
    }
}