package com.preproom.engine.session;

import com.preproom.engine.interview.InterviewKit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Progress is computed in Java from stored attempts. The best attempt per question counts. */
public final class ProgressCalculator {

    private ProgressCalculator() {}

    public static Progress of(SessionDoc session) {
        Map<Integer, Integer> best = new HashMap<>();
        if (session.attempts() != null) {
            for (SessionDoc.Attempt a : session.attempts()) {
                if ("GRADED".equals(a.status()) && a.score() != null) {
                    best.merge(a.questionId(), a.score(), Math::max);
                }
            }
        }
        List<InterviewKit.KitQuestion> questions = session.questions() == null ? List.of() : session.questions();
        Map<Integer, String> requirementById = new HashMap<>();
        if (session.gap() != null && session.gap().assessments() != null) {
            session.gap().assessments().forEach(a -> requirementById.put(a.id(), a.requirement()));
        }
        List<Progress.WeakSpot> weak = new ArrayList<>();
        for (InterviewKit.KitQuestion q : questions) {
            Integer score = best.get(q.id());
            if (score != null && q.requirementId() != null && requirementById.containsKey(q.requirementId())) {
                weak.add(new Progress.WeakSpot(requirementById.get(q.requirementId()), score));
            }
        }
        weak.sort(Comparator.comparingInt(Progress.WeakSpot::bestScore));
        Integer average = best.isEmpty() ? null
                : (int) Math.round(best.values().stream().mapToInt(Integer::intValue).average().orElse(0));
        return new Progress(best.size(), questions.size(), average, weak.stream().limit(3).toList());
    }
}