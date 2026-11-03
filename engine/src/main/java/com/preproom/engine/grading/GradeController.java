package com.preproom.engine.grading;

import com.preproom.engine.document.AiExtractionException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GradeController {

    public record GradeRequest(String question, List<String> points, String answer) {}

    private final GradeService service;
    private final boolean enabled;

    public GradeController(GradeService service,
                           @Value("${preproom.llmtest.enabled:false}") boolean enabled) {
        this.service = service;
        this.enabled = enabled;
    }

    @PostMapping("/api/interview/grade")
    public GradeResult grade(@RequestBody GradeRequest request) {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        try {
            return service.grade(request.question(), request.points(), request.answer());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AiExtractionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }
}