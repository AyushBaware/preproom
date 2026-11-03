package com.preproom.engine.session;

import com.preproom.engine.gap.GapReport;
import com.preproom.engine.gap.GapService;
import com.preproom.engine.grading.GradeResult;
import com.preproom.engine.grading.GradeService;
import com.preproom.engine.interview.InterviewKit;
import com.preproom.engine.interview.KitService;
import com.preproom.engine.jd.ExtractionResult;
import com.preproom.engine.jd.JdExtractionService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    public record Created(String sessionId, String roleTitle, GapReport gap, InterviewKit kit) {}

    public record Answered(GradeResult grade, Progress progress) {}

    public record SessionView(String id, String roleTitle, GapReport gap, List<InterviewKit.KitQuestion> questions,
                              List<SessionDoc.Attempt> attempts, Progress progress) {}

    private final JdExtractionService jdService;
    private final GapService gapService;
    private final KitService kitService;
    private final GradeService gradeService;
    private final SessionRepository repository;

    public SessionService(JdExtractionService jdService, GapService gapService, KitService kitService,
                          GradeService gradeService, SessionRepository repository) {
        this.jdService = jdService;
        this.gapService = gapService;
        this.kitService = kitService;
        this.gradeService = gradeService;
        this.repository = repository;
    }

    public Created create(String jdText, String resumeText) {
        ExtractionResult jd = jdService.extract(jdText);
        GapReport gap = gapService.analyze(jd.jd(), resumeText);
        InterviewKit kit = kitService.generate(jd.jd(), gap, jdText);
        String id = UUID.randomUUID().toString();
        repository.save(new SessionDoc(id, Instant.now(), jd.jd().roleTitle(), stripEvidence(gap),
                kit.questions(), List.of()));
        return new Created(id, jd.jd().roleTitle(), gap, kit);
    }

    public Answered answer(String sessionId, Integer questionId, String answerText) {
        if (questionId == null) {
            throw new IllegalArgumentException("questionId is required");
        }
        SessionDoc session = find(sessionId);
        InterviewKit.KitQuestion question = session.questions().stream()
                .filter(q -> q.id() == questionId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No question with that number in this session"));
        GradeResult grade = gradeService.grade(question.text(), question.goodAnswerPoints(), answerText);
        List<SessionDoc.PointOutcome> outcomes = grade.points().stream()
                .map(p -> new SessionDoc.PointOutcome(p.index(), p.point(), p.coverage(), p.feedback())).toList();
        SessionDoc updated = session.withAttempt(
                new SessionDoc.Attempt(questionId, Instant.now(), grade.status(), grade.score(), outcomes));
        repository.save(updated);
        return new Answered(grade, ProgressCalculator.of(updated));
    }

    public SessionView view(String sessionId) {
        SessionDoc s = find(sessionId);
        return new SessionView(s.id(), s.roleTitle(), s.gap(), s.questions(),
                s.attempts() == null ? List.of() : s.attempts(), ProgressCalculator.of(s));
    }

    public void delete(String sessionId) {
        find(sessionId);
        repository.deleteById(sessionId);
    }

    private SessionDoc find(String sessionId) {
        return repository.findById(sessionId).orElseThrow(() -> new NoSuchElementException("Session not found"));
    }

    /** Evidence quotes are the candidate's own resume words, so they are not stored. */
    static GapReport stripEvidence(GapReport gap) {
        List<GapReport.Assessment> stripped = gap.assessments().stream()
                .map(a -> new GapReport.Assessment(a.id(), a.level(), a.requirement(), a.verdict(), a.reason(), List.of()))
                .toList();
        return new GapReport(stripped, gap.matches(), gap.partials(), gap.gaps());
    }
}