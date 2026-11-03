package com.preproom.engine.session;

import com.preproom.engine.document.AiExtractionException;
import com.preproom.engine.document.PdfTextExtractor;
import java.io.IOException;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SessionController {

    public record AnswerRequest(Integer questionId, String answer) {}

    private final SessionService service;
    private final boolean enabled;

    public SessionController(SessionService service, @Value("${preproom.llmtest.enabled:false}") boolean enabled) {
        this.service = service;
        this.enabled = enabled;
    }

    @PostMapping("/api/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionService.Created create(@RequestParam("jd") MultipartFile jd,
                                         @RequestParam("resume") MultipartFile resume) throws IOException {
        requireEnabled();
        try {
            return service.create(PdfTextExtractor.extract(jd.getBytes()), PdfTextExtractor.extract(resume.getBytes()));
        } catch (RuntimeException e) {
            throw translate(e);
        }
    }

    @PostMapping("/api/sessions/{id}/answers")
    public SessionService.Answered answer(@PathVariable String id, @RequestBody AnswerRequest request) {
        requireEnabled();
        try {
            return service.answer(id, request.questionId(), request.answer());
        } catch (RuntimeException e) {
            throw translate(e);
        }
    }

    @GetMapping("/api/sessions/{id}")
    public SessionService.SessionView view(@PathVariable String id) {
        requireEnabled();
        try {
            return service.view(id);
        } catch (RuntimeException e) {
            throw translate(e);
        }
    }

    @DeleteMapping("/api/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        requireEnabled();
        try {
            service.delete(id);
        } catch (RuntimeException e) {
            throw translate(e);
        }
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private static RuntimeException translate(RuntimeException e) {
        if (e instanceof IllegalArgumentException) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        if (e instanceof NoSuchElementException) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        if (e instanceof AiExtractionException) {
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
        if (e instanceof DataAccessException) {
            return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The database is unavailable. Check the Atlas IP list and try again.");
        }
        return e;
    }
}